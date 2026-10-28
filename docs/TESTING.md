# Тестирование

## 1. Принципы

- Unit-тесты не требуют Docker, PostgreSQL, MinIO или Elasticsearch.
- Интеграционные тесты репозиториев и контекста используют H2 в режиме
  PostgreSQL-совместимости и Liquibase.
- Тестовая схема создаётся теми же changeset, что и production, и проверяется
  Hibernate (`ddl-auto=validate`). Это ловит расхождения миграций и сущностей.
- Внешние сервисы (MinIO, SMTP, Elasticsearch, Quartz Scheduler) в unit-тестах
  заменяются Mockito.
- Тесты фиксируют не только happy path, но и отказы: чужая версия, повторная
  подпись, недопустимый переход статуса, отсутствие документа.

## 2. Команды

```bash
cd backend
./mvnw -ntp clean verify
```

Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd -ntp clean verify
```

Только тесты без упаковки:

```bash
./mvnw -ntp test
```

Один класс:

```bash
./mvnw -ntp -Dtest=SignatureServiceImplTest test
```

Несколько классов (PowerShell требует кавычек):

```powershell
.\mvnw.cmd -ntp "-Dtest=SignatureServiceImplTest,DocumentServiceImplAdditionalTest" test
```

## 3. Покрытие

JaCoCo включён в `verify` и падает при покрытии строк ниже **90%**:

```bash
backend/target/site/jacoco/index.html
```

Исключены из расчёта:

- `BackendApplication`;
- `configurations` (инфраструктурные бины);
- `dto`, `entity`, `enums`, `exceptions`;
- `repository` (проверяются интеграционными тестами, но не считаются бизнес-кодом);
- сгенерированные `mapper/**/*Impl.class`.

Текущее покрытие после рефакторинга: **92.37% строк** (1235/1337), **80.25% ветвей**
(260/324), **356 тестов**. Порог фиксирует минимум и не позволяет снизить покрытие
незаметно.

## 4. Уровни тестов

### Unit-тесты сервисов

- `SignatureServiceImplTest` — подписание, повторная подпись, недопустимый статус,
  отсутствующая заявка, SHA-256.
- `AuthorizationServiceImplTest` — владелец/посторонний, отсутствующий документ,
  null/пустой `contentUrl`, доступ к скачиванию.
- `DocumentServiceTests`,
  `DocumentServiceImplAdditionalTest` — создание, обновление, удаление,
  архивирование, пагинация, доступ владельца/подписанта, отправка на подписание,
  скачивание.
- `ApprovementServiceImplTest` — участники, делегирование, дубликаты, отправка на
  голосование.
- `VotingServiceImplTest` — планирование Quartz, ошибка планирования, подсчёт
  голосов, идемпотентность.
- `DelegationServiceImplTest`, `DepartmentServiceImplTest`,
  `ReplacementManagementServiceImplTest` — организационная модель и замещения.
- `AttributeServiceImplTest`, `DocumentAttributeValueServiceImplTest`,
  `DocumentVersionServiceImplTest` — атрибуты, версии, поиск.
- `DocumentBaseStateTest` — переходы state machine для всех статусов.
- `AuthServiceImplTest`, `SecurityServicesTest`, `JwtFilterTest` — вход, JWT,
  refresh, публичные пути, 401/403.
- `AnalyticsServiceImplTest`, `EmailServiceImplTest`,
  `InfrastructureComponentsTest`, `MapperDefaultMethodsTest` — аналитика,
  уведомления, bootstrap, инфраструктура.

### Интеграционные тесты репозиториев

`@DataJpaTest` + `@ActiveProfiles("test")`:

- Liquibase создаёт схему;
- Hibernate проверяет её;
- проверяются запросы документов, доступ по `contentUrl`, сортировки, страницы,
  проекции и ограничения.

### Контекстный тест

`BackendApplicationTests` поднимает полный Spring-контекст без Docker. Elasticsearch
repository мокается, MinIO и переиндексация отключены флагами. Это проверяет
конфигурацию, бины, Liquibase и security-цепочку.

### Контроллеры

`@WebMvcTest` проверяет коды ответов, валидацию DTO, JSON и вызовы сервисов.
Скачивание проверяется отдельно: разрешённый пользователь получает ссылку,
запрещённый — 403 и не получает presigned URL.

## 5. Сценарии подписания (обязательные)

| № | Сценарий | Ожидание |
|---|---|---|
| 1 | Автор отправляет версию на подписание | создаётся заявка, событие уведомления |
| 2 | Участник подписывает последнюю версию | статус заявки и версии меняется, подпись сохранена |
| 3 | Повторная подпись той же заявки | 409, статусы и подпись не меняются |
| 4 | Подпись несуществующей заявки | 404 |
| 5 | Подпись чужого документа | 403/404, без изменения данных |
| 6 | Недопустимый переход статуса | 409, транзакция откатывается |
| 7 | Подписание не последней версии | отклоняется |
| 8 | Делегирование подписи | подписывает замещающий пользователь |
| 9 | Завершение всех подписей | процесс переходит к голосованию/результату |
| 10 | Ошибка планирования Quartz | исключение, создание процесса откатывается |
| 11 | Повторный сбор результатов | идемпотентно, без дублей |
| 12 | Скачивание файла участником | ссылка выдаётся |
| 13 | Скачивание файла посторонним | 403, ссылка не генерируется |
| 14 | JWT истёк, refresh валиден | `/jwt/update` доступен без access-токена |

Эти сценарии распределены между `SignatureServiceImplTest`,
`DocumentServiceImplAdditionalTest`, `DocumentControllerTest`,
`VotingServiceImplTest`, `DelegationServiceImplTest` и `JwtFilterTest`.

## 6. Полный стек в VirtualBox

Docker Desktop может быть недоступен; ниже — воспроизводимая проверка на VM.

### Подготовка

1. VirtualBox: Ubuntu Server 22.04/24.04, 4 vCPU, 8 ГБ RAM, 40 ГБ диска.
2. NAT + проброс порта 8080 (и 9000 при необходимости).
3. Установить Docker Engine и Compose plugin.
4. Клонировать репозиторий и перейти на проверяемую ветку.

### Запуск

```bash
cp .env.example .env
# заполнить POSTGRES_*, JWT_SECRET, MINIO_*, CORS_ALLOWED_ORIGINS
docker compose --env-file .env -f docker/docker-compose-all.yaml up -d --build
docker compose --env-file .env -f docker/docker-compose-all.yaml ps
```

Проверить:

```bash
curl -fsS http://localhost:8080/api/v1/actuator/health
```

Ожидается `{"status":"UP"}`. Swagger:
`http://<vm-host>:8080/api/v1/swagger-ui/index.html`.

### Smoke-сценарий

1. Создать администратора через bootstrap-переменные или войти существующим.
2. Создать тип документа и атрибуты.
3. Создать документ, загрузить файл, проверить версию.
4. Отправить версию на подписание другому пользователю.
5. Войти подписавшим, подписать, проверить статус.
6. Повторить подпись — получить 409.
7. Скачать файл владельцем и подписантом — 200.
8. Попробовать скачать файл посторонним — 403.
9. Проверить `/jwt/update` с истёкшим access-токеном и валидным refresh.
10. Перезапустить контейнеры и убедиться, что данные и файлы сохранились.

Финальная проверка на Compose после выноса авторизации: health `UP`, скачивание
владельцем — 200, presigned URL — 200, посторонним — 403. Полный сценарий
подписания, повторная подпись (409), решения голосования и сохранность данных
после перезапуска проверены ранее на том же стенде.

### Ограничения VM

- Elasticsearch требует памяти; при 4 ГБ RAM возможны OOM — увеличьте VM или
  отключите reindex.
- SMTP может быть недоступен; уведомления не должны ломать основной сценарий.
- Не используйте production-секреты в VM.

## 7. Отладка тестов

| Проблема | Решение |
|---|---|
| `Schema-validation: missing table` | проверьте URL H2 без `DATABASE_TO_UPPER=false`; Liquibase должен применить схему до JPA |
| `Port already in use` | остановите локальный PostgreSQL/Elasticsearch или измените порт |
| Тест зависит от порядка | используйте фиксированные `createdAt`/`id`, не полагайтесь на порядок БД |
| `Strict stubbing` в Mockito | удалите неиспользуемый `when`, проверьте аргументы |
| JaCoCo ниже 90% | добавьте тест на непокрытую ветку, не понижайте порог |
| Тест требует Docker | это ошибка: unit/integration тесты должны работать без Docker |

## 8. Что проверяется в CI

`./mvnw -B -ntp clean verify`:

- компиляция;
- unit/интеграционные тесты;
- Liquibase + H2 + `ddl-auto=validate`;
- JaCoCo threshold;
- сборка jar;
- публикация отчётов и артефактов при падении.

Docker-сборка и деплой запускаются только для `main` после успешных тестов.
