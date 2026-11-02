# CaseLab EDM

CaseLab EDM — система электронного документооборота: документы и версии,
согласование, подписание, голосование, делегирование, атрибуты, аналитика и
уведомления.

Репозиторий содержит **два запускаемых сервиса** и один переиспользуемый
технический стартер:

| Модуль | Назначение | Порт | Контекст |
|---|---|---|---|
| `security-service` | identity-сервис: пользователи, роли, подразделения, JWT, refresh-токены | `8081` | `/api/v1` |
| `backend` | бизнес-API документооборота | `8080` | `/api/v1` |
| `platform-spring-boot-starter` | общие технические интеграции: JWT-фильтр, resource-server, MinIO, mail, Elasticsearch | — | — |

`backend` не содержит JPA-сущностей пользователей и подразделений и не
подключается к базе `security-service`. Пользователи представлены UUID и
read-only проекцией `UserSummary`, которую backend получает по внутреннему HTTP
API. Публичные URL бизнес-API при разделении сервисов сохранены.

## Содержание

- [Стек](#стек)
- [Структура](#структура)
- [Архитектура](#архитектура)
- [Быстрый старт](#быстрый-старт)
- [Запуск без полного Docker](#запуск-без-полного-docker)
- [Конфигурация](#конфигурация)
- [Миграции БД](#миграции-бд)
- [Тесты и покрытие](#тесты-и-покрытие)
- [Автоматический деплой](#автоматический-деплой)
- [Безопасность](#безопасность)
- [Документация](#документация)

## Стек

- Java 17, Spring Boot 3
- Spring Security + JWT, Spring Data JPA, Liquibase
- PostgreSQL 14: отдельные базы для `security-service` и `backend`
- MinIO (S3-совместимое хранилище файлов)
- Elasticsearch 8
- Maven Wrapper, JUnit 5, Mockito, AssertJ, H2 для тестов
- Docker / Docker Compose, GitHub Actions

## Структура

```text
.
├── backend/                      # бизнес-контексты документооборота
│   ├── src/main/java/ru/caselab/edm/backend/
│   │   ├── document/             # документы, версии, типы, атрибуты
│   │   ├── approval/             # согласование, подписание, голосование
│   │   ├── delegation/           # замещение и делегирование
│   │   ├── analytics/            # read model и отчёты
│   │   ├── integration/          # Elasticsearch, MinIO, mail
│   │   └── shared/               # directory, security, exceptions, config
│   └── src/main/resources/liquibase/
├── security-service/             # отдельное identity-приложение
│   ├── src/main/java/ru/caselab/edm/security/
│   │   ├── api/                  # /auth, /jwt, /users, /department
│   │   ├── internal/             # /internal/** для backend
│   │   ├── domain/               # User, Department, Role, RefreshToken
│   │   └── persistence/
│   └── src/main/resources/liquibase/
├── platform-spring-boot-starter/ # общие интеграции без бизнес-логики
├── docker/                       # compose для локального и production-запуска
└── docs/                         # архитектура, DDD, тесты, деплой, безопасность
```

## Архитектура

```text
Client
  │ HTTPS + access JWT
  ├──────────────────────────────► backend :8080/api/v1
  │                                  │  UUID пользователя/роли из JWT
  │                                  ├── PostgreSQL edm
  │                                  ├── MinIO
  │                                  ├── Elasticsearch
  │                                  └── UserDirectory ──► security-service :8081/api/v1
  │                                                           │ X-Internal-Token
  │                                                           └── PostgreSQL security
  │
  └── /auth, /jwt/update ────────► security-service
```

- Access-token содержит `uid`, `roles`, `department_id`,
  `managed_department_id` и `type=access`. Оба сервиса используют один
  `JWT_SECRET`, но проверяют токен локально, без обращения в БД.
- Backend получает профиль пользователя только через `UserDirectory` и
  `/internal/**`. Внутренние endpoint-ы защищены заголовком
  `X-Internal-Token`.
- Бизнес-сущности хранят UUID-ссылки: `Document.ownerId`,
  `ApprovementProcessItem.signerId`,
  `ReplacementManager.managerUserId/tempManagerUserId`.
- Resource-authorization контракты (`ResourceAccessPolicy` и
  `AuthorizationService`) находятся в backend, а identity-сервис не знает о
  документах.
- `platform-spring-boot-starter` содержит только технические интеграции и
  автоконфигурации; JPA-сущности и бизнес-правила туда не входят.

Подробнее: [ARCHITECTURE.md](docs/ARCHITECTURE.md) и [DDD.md](docs/DDD.md).

## Быстрый старт

Требуются Docker Engine/Desktop с Compose plugin.

```bash
cp .env.example .env
# заполнить все change-me значения; JWT_SECRET сгенерировать:
# openssl rand -base64 64
docker compose --env-file .env -f docker/docker-compose-all.yaml up -d --build
```

Проверка:

```bash
curl -fsS http://localhost:8081/api/v1/actuator/health
curl -fsS http://localhost:8080/api/v1/actuator/health
```

Ожидается `{"status":"UP"}` для обоих сервисов.

- Backend Swagger: `http://localhost:8080/api/v1/swagger-ui/index.html`
- Security Swagger: `http://localhost:8081/api/v1/swagger-ui/index.html`
- MinIO console: `http://localhost:9090`

Если база пустая и нужен первый администратор, временно установите
`BOOTSTRAP_ADMIN_ENABLED=true` и заполните `BOOTSTRAP_ADMIN_*`. Bootstrap
выполняется **security-service**, создаёт пользователя и роль `ADMIN`. После
первого успешного входа выключите bootstrap и перезапустите сервис.

## Запуск без полного Docker

Можно поднять только инфраструктуру и запустить приложения из IDE:

```bash
docker compose --env-file .env -f docker/docker-compose-services.yaml up -d
```

Порты инфраструктуры:

| Сервис | Адрес |
|---|---|
| PostgreSQL backend | `localhost:5432` |
| PostgreSQL security | `localhost:5433` |
| MinIO | `localhost:9000` |
| Elasticsearch | `localhost:9200` |

Запустите `SecurityServiceApplication`, затем `BackendApplication`. Оба
приложения читают переменные окружения из `.env` (или из run configuration).
`DATABASE_URL` указывает на базу `edm`, `SECURITY_DATABASE_URL` — на базу
`security`.

## Конфигурация

Все секреты передаются только через переменные окружения. В репозитории
допустимы лишь заглушки `change-me-*`.

| Группа | Переменные |
|---|---|
| Backend БД | `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` |
| Security БД | `SECURITY_DATABASE_URL`, `SECURITY_DATABASE_USERNAME`, `SECURITY_DATABASE_PASSWORD` |
| JWT | `JWT_SECRET`, `JWT_ACCESS_TOKEN_TTL`, `JWT_REFRESH_TOKEN_TTL` |
| Межсервисное взаимодействие | `SECURITY_SERVICE_URL`, `SECURITY_INTERNAL_TOKEN` |
| CORS | `CORS_ALLOWED_ORIGINS` |
| MinIO | `MINIO_ENDPOINT`, `MINIO_PUBLIC_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET` |
| Elasticsearch | `ELASTIC_ENDPOINT`, `ELASTIC_REINDEX_ON_STARTUP` |
| Mail | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` |
| Bootstrap администратора | `BOOTSTRAP_ADMIN_ENABLED`, `BOOTSTRAP_ADMIN_LOGIN`, `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD` |

Backend использует префиксы `platform.security.*`, `platform.storage.minio.*`,
`platform.search.*`, `platform.mail.*` и `security.service.*`.

`JWT_SECRET` должен быть одинаковым для двух сервисов, но не совпадать с
паролями БД, SMTP или MinIO. `SECURITY_INTERNAL_TOKEN` — отдельный случайный
секрет; без него backend не сможет обращаться к directory API.

## Миграции БД

Liquibase управляет схемой; `ddl-auto=validate` проверяет соответствие сущностей
схеме при старте.

- `security-service/src/main/resources/liquibase/` — `users`, `roles`,
  `departments`, `refresh_token` и связи между ними.
- `backend/src/main/resources/liquibase/` — `documents`, версии, атрибуты,
  согласование, подписи, замещения и аналитика.

Backend-схема не содержит таблиц `users`, `roles` и `refresh_token`, а также
внешних ключей на identity-базу. Обе базы мигрируют независимо.

## Тесты и покрытие

```bash
./mvnw -ntp clean verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd -ntp clean verify
```

Тесты не требуют Docker: repository-тесты используют H2 и ту же Liquibase-схему.
JaCoCo проверяет минимум **90% строк отдельно** для
`platform-spring-boot-starter`, `security-service` и `backend`.

Отчёты:

- `platform-spring-boot-starter/target/site/jacoco/index.html`
- `security-service/target/site/jacoco/index.html`
- `backend/target/site/jacoco/index.html`

Подробнее: [TESTING.md](docs/TESTING.md).

## Автоматический деплой

Штатный путь выпуска — merge/push в `main`. GitHub Actions выполняет:

1. `clean verify` всего reactor;
2. сборку и публикацию двух Docker-образов;
3. автоматический деплой на сервер;
4. health-check обоих сервисов.

Push и pull request в `dev` запускают тесты, но не деплой. Ручной запуск
production Compose не является штатным способом выпуска. Необходимые secrets,
переменные и правила отката описаны в [DEPLOYMENT.md](docs/DEPLOYMENT.md).

## Безопасность

- Не коммитьте `.env`, пароли, JWT-секреты, SMTP/MinIO-ключи и приватные
  SSH-ключи.
- Считать все ранее опубликованные значения скомпрометированными и
  ротировать их.
- Внутренние endpoint-ы `/internal/**` доступны только с
  `X-Internal-Token`; не публикуйте их через reverse proxy.
- Пароли хранятся в BCrypt, refresh-токены — в БД с возможностью отзыва.
- Файлы MinIO не публичны; выдаются presigned URL с ограниченным TTL.
- Проверка владельца/подписанта выполняется в backend до выдачи файла.

Подробнее: [SECURITY.md](docs/SECURITY.md).

## Документация

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) — компоненты и потоки запросов.
- [DDD.md](docs/DDD.md) — bounded contexts, агрегаты и инварианты.
- [TESTING.md](docs/TESTING.md) — тесты, покрытие и smoke-сценарий.
- [DEPLOYMENT.md](docs/DEPLOYMENT.md) — CI/CD и production-окружение.
- [SECURITY.md](docs/SECURITY.md) — секреты, авторизация и инциденты.
- [REFACTORING_PLAN.md](docs/REFACTORING_PLAN.md) — история рефакторинга.
