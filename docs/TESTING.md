# Тестирование

## 1. Принципы

- Unit- и slice-тесты не требуют Docker, PostgreSQL, MinIO или Elasticsearch.
- Repository-тесты поднимают H2 и ту же Liquibase-схему, что и production, с
  `ddl-auto=validate`.
- Identity-база и backend-база тестируются раздельно; backend-тесты не содержат
  таблиц `users`/`departments`.
- Внешние сервисы заменяются Mockito, HTTP-клиент identity — MockRestServiceServer.
- Проверяются не только happy path, но и отказы: чужой документ, повторная
  подпись, невалидные DTO, истёкшие токены, недоступный сервис.
- ArchUnit проверяет, что `security-service` не зависит от backend.

## 2. Запуск

Сборка из корня проверяет все модули:

```bash
./mvnw -ntp clean verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd -ntp clean verify
```

Только тесты:

```bash
./mvnw -ntp test
```

Один тест или класс:

```bash
./mvnw -ntp -pl backend -Dtest=SignatureServiceImplTest test
./mvnw -ntp -pl security-service -Dtest=InternalTokenFilterTest test
./mvnw -ntp -pl platform-spring-boot-starter -Dtest=JwtTokenServiceImplTest test
```

Несколько тестов (PowerShell требует кавычек):

```powershell
.\mvnw.cmd -ntp -pl backend "-Dtest=SignatureServiceImplTest,DocumentServiceImplAdditionalTest" test
```

Если модуль зависит от starter, используйте `-am`:

```bash
./mvnw -ntp -pl backend -am test
```

## 3. Покрытие

JaCoCo запускается на `verify` и валидирует минимум **90% строк отдельно для
каждого модуля**:

- `platform-spring-boot-starter/target/site/jacoco/index.html`;
- `security-service/target/site/jacoco/index.html`;
- `backend/target/site/jacoco/index.html`.

Исключены из coverage: bootstrap/configuration, DTO, JPA-сущности, enum,
exception, repository-интерфейсы и сгенерированные MapStruct-реализации.
Новый технический код (JWT, фильтры, ObjectStorage, SMTP, UserDirectory)
покрывается тестами, а не исключается.

Текущий baseline:

| Модуль | Тесты | Строки |
|---|---:|---:|
| `platform-spring-boot-starter` | 40 | 98.62% (286/290) |
| `security-service` | 117 | 93.08% (363/390) |
| `backend` | 266 | 92.97% (860/925) |
| **Итого** | **423** | — |

## 4. Что покрыто

### Platform starter

- `JwtTokenServiceImplTest` — access/refresh claims, TTL, невалидные токены.
- `JwtAuthenticationFilterTest` — публичные пути, невалидный/просроченный
  токен, JSON 401.
- `PlatformSecurityIntegrationTest` — security chain и `AuthenticatedUser`.
- `AutoConfigurationTest` — условная активация JWT/MinIO/mail/Elastic.
- `MinioObjectStorageTest` — загрузка, presigned URL, удаление.
- `SmtpEmailSenderTest` — SMTP-письма без реальной почты.

### Security service

- `AuthServiceImplTest` — вход, выдача access/refresh.
- `SecurityJwtServicesTest` — refresh, claims, роли.
- `UserServiceTests` — CRUD пользователей, пароли, конфликты.
- `DepartmentServiceImplTest` — подразделения, менеджеры, участники.
- `InternalUserServiceTest` — проекции для backend и replacement candidates.
- `InternalTokenFilterTest` — доступ к `/internal/**` только с токеном.
- `InternalUserControllerTest` — internal HTTP-контракт.
- `UserControllerTest`, `DepartmentControllerTest` — публичные MVC-контракты.
- `SecurityArchitectureTest` — запрет зависимостей security → backend.

### Backend

- `DocumentServiceTests`, `DocumentServiceImplAdditionalTest` — создание,
  обновление, удаление, отправка на подписание, проверка владельца.
- `DocumentControllerTest`, `DocumentTypeControllerTest` — HTTP-контракты,
  включая исторические `/document` URL approval-сценариев.
- `SignatureServiceImplTest` — подпись, запрет повторной подписи, SHA-256.
- `ApprovementServiceImplTest` — согласование, участники, дубликаты.
- `VotingServiceImplTest` — Quartz, дедлайн, идемпотентный сбор результатов.
- `DelegationServiceImplTest`, `ReplacementManagementServiceImplTest` —
  замещение, проверка подразделения, пагинация.
- `AttributeServiceImplTest`, `DocumentAttributeValueServiceImplTest`,
  `DocumentVersionServiceImplTest` — атрибуты и версии.
- `DocumentBaseStateTest` — переходы state machine.
- `DocumentResourceAccessPolicyTest`, `ResourceAuthorizationServiceImplTest` —
  resource-authorization.
- `HttpUserDirectoryClientTest` — контракт `/internal/**`, ошибки и заголовок
  `X-Internal-Token`.
- `ElasticsearchReindexRunnerTest`, `EmailServiceImplTest` — интеграционные
  адаптеры.
- `DocumentRepositoryTest`, `AnalyticsRepositoryTest`,
  `ReplacementManagerRepositoryTest` — H2 + Liquibase + Hibernate validate.

## 5. Smoke-сценарий

Полный стек запускается по [README](../README.md). После `up -d --build`
проверьте:

1. `GET /api/v1/actuator/health` на `8081` и `8080` возвращает `UP`.
2. Bootstrap создаёт администратора в `security-service`.
3. `POST /api/v1/auth` с логином/паролем выдаёт access и refresh.
4. `POST /api/v1/jwt/update` обновляет access по refresh.
5. Backend принимает access-токен без запроса в identity-БД.
6. Создание документа, загрузка версии и проверка атрибутов.
7. Отправка на подписание, подпись автора и контрагента.
8. Повторная подпись возвращает `409`.
9. Чужой пользователь не может скачать файл (403).
10. Делегирование подписи в активном периоде.
11. Голосование с дедлайном и идемпотентный сбор результатов.
12. Данные в обеих PostgreSQL и MinIO сохраняются после перезапуска.

## 6. Типовые проблемы

| Сообщение | Решение |
|---|---|
| `Schema-validation: missing table` | проверить, что Liquibase-схема нужного сервиса применилась до JPA |
| `Port already in use` | освободить 8080/8081/5432/5433/9000/9200 |
| `Strict stubbing` в Mockito | убрать лишние `when`, проверить аргументы |
| JaCoCo ниже 90% | добавить тесты на отказные сценарии, не понижать порог |
| context-тест падает без Docker | тест должен использовать H2 и моки, не Testcontainers |
| 401 на `/internal/**` | одинаковый `SECURITY_INTERNAL_TOKEN` в двух сервисах |
| `UserDirectoryException` | проверить `SECURITY_SERVICE_URL`, health identity и логи |
