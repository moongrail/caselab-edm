# Архитектура CaseLab EDM

## 1. Назначение

CaseLab EDM — система электронного документооборота. Репозиторий содержит два
запускаемых Spring Boot приложения и один технический starter:

| Компонент | Тип | Ответственность |
|---|---|---|
| `security-service` | отдельный микросервис | identity: пользователи, роли, подразделения, аутентификация, JWT/refresh, внутренний directory API |
| `backend` | отдельный микросервис | документы, версии, атрибуты, согласование, подписание, голосование, делегирование, аналитика |
| `platform-spring-boot-starter` | библиотека | JWT-кодек, JWT-фильтр, resource-server, MinIO, SMTP, Elasticsearch reindex; без бизнес-сущностей |

Публичные URL бизнес-API сохранены: backend по-прежнему обслуживает
`/api/v1/document/**`, `/api/v1/attributes/**`, `/api/v1/analytics/**`,
`/api/v1/replacement_managers/**`. Identity API находится в отдельном приложении:
`/api/v1/auth`, `/api/v1/jwt`, `/api/v1/users`, `/api/v1/department`.

## 2. Компоненты и порты

```text
                    ┌────────────────────────────┐
Browser / Mobile ──►│ backend :8080/api/v1       │
                    │ Spring MVC + @PreAuthorize │
                    └───────┬───────────┬────────┘
                            │           │
             JWT-local check│           │ UserDirectory (HTTP)
                            │           │ X-Internal-Token
                            │           ▼
                            │   ┌────────────────────────────┐
                            │   │ security-service :8081     │
                            │   │ /api/v1/auth|jwt|users|... │
                            │   └──────────┬─────────────────┘
                            │              │ JPA + Liquibase
                            │              ▼
                            │        PostgreSQL security
                            │
          ┌─────────────────┼─────────────────────┐
          ▼                 ▼                     ▼
   PostgreSQL edm         MinIO             Elasticsearch
```

Access-токен проверяется каждым сервисом локально. Обращение к
`security-service` в happy path не требуется: backend извлекает `uid`, `roles`,
`department_id`, `managed_department_id` из подписанного JWT.

## 3. Границы сервисов

### 3.1. Security Service

- `api` — публичные `/auth`, `/jwt/update`, `/users`, `/department`.
- `internal` — `/internal/users/**` и `/internal/departments/**` для backend.
- `domain` — `User`, `Department`, `Role`, `RefreshToken`, инварианты ролей и
  подразделений.
- `persistence` — JPA-репозитории identity.
- `jwt` — выдача access/refresh токенов.
- `config` — security chain, CORS, bootstrap администратора, фильтр
  `X-Internal-Token`.

Внутренние endpoint-ы не предназначены для внешних клиентов. Фильтр
`InternalTokenFilter` сравнивает заголовок с `security.internal.token` и
возвращает `401`, если токен пуст/неверен. В production reverse proxy не должен
маршрутизировать `/internal/**` наружу.

### 3.2. Backend

Разбит по bounded contexts:

```text
ru.caselab.edm.backend
├── document/       # Document, DocumentVersion, DocumentType, Attribute
├── approval/       # ApprovementProcess, Signature, Voting
├── delegation/     # ReplacementManager, делегирование подписи
├── analytics/      # read model и агрегаты
├── integration/    # Elasticsearch, MinIO, mail adapters
└── shared/
    ├── directory/  # UserDirectory + HTTP-клиент security-service
    ├── security/   # ResourceAccessPolicy, AuthorizationService
    ├── exception/  # единый error contract
    └── config/     # OpenAPI, Jackson, scheduler
```

`ApprovalController` физически находится в `approval/api`, но сохраняет
исторические URL `/document/approvement/**`, `/document/{id}/sign`,
`/document/{id}/send_for_signature`. Это осознанная совместимость публичного
контракта.

### 3.3. Platform Starter

Starter предоставляет только переиспользуемые технические адаптеры:

- `security` — `JwtTokenService`, `JwtAuthenticationFilter`,
  `AuthenticatedUser`, resource-server;
- `storage` — интерфейс `ObjectStorage` и MinIO-реализация;
- `mail` — `EmailSender` и SMTP-реализация;
- `search` — `ElasticsearchReindexRunner`;
- `autoconfigure` — Spring Boot auto-configuration с conditionals.

Starter не содержит identity-сущностей, репозиториев или бизнес-правил
документооборота. Оба приложения подключают его как зависимость и получают
согласованную JWT-конфигурацию.

## 4. Аутентификация и авторизация

Access-token:

```json
{
  "uid": "UUID",
  "sub": "login",
  "roles": ["USER", "ADMIN"],
  "department_id": 1,
  "managed_department_id": 2,
  "type": "access",
  "iat": 0,
  "exp": 0
}
```

- `platform.security.jwt.secret` должен совпадать в двух сервисах.
- Backend создаёт `AuthenticatedUser` из claims, без запроса в БД.
- Роли проверяются `@PreAuthorize` на уровне API.
- Владение документами и доступ к файлам проверяются
  `AuthorizationService`/`ResourceAccessPolicy`.
- Refresh-токен обрабатывается только `security-service`; он хранится в БД и
  может быть отозван.

## 5. Данные

### 5.1. PostgreSQL

У сервисов разные базы и разные credentials:

- `edm` — таблицы документов, версий, атрибутов, согласования, подписей,
  замещений.
- `security` — `users`, `roles`, `departments`, `user_roles`,
  `refresh_token`, `department_managers`, `department_members`.

В backend нет JPA-сущностей `User`, `Department`, `Role`. Нет и внешних ключей
на identity-таблицы. Ссылки на пользователей — UUID-колонки
`documents.owner_id`, `approvment_process_item.signer_id`,
`replacement_managers.manager_user_id`,
`replacement_managers.temp_manager_user_id`. Целостность ссылок обеспечивается
приложением и identity API, а не межбазовым FK.

### 5.2. UserDirectory

`shared/directory/UserDirectory` — ACL между backend и identity:

- `requireUser(UUID)` — профиль пользователя;
- `findUsers(Collection<UUID>)` — пакетное обогащение ответов;
- `replacementCandidates(UUID)` — кандидаты на замещение;
- `departmentMembers(...)` — участники подразделения.

HTTP-клиент передаёт `X-Internal-Token`, маппит `404` в
`ResourceNotFoundException`, остальные ошибки — в `UserDirectoryException`.
Сетевой вызов изолирован от доменной модели.

### 5.3. MinIO и Elasticsearch

Backend хранит файлы в MinIO через `ObjectStorage`; в БД сохраняется имя
объекта. Наружу выдаётся presigned URL с TTL через публичный endpoint.
Elasticsearch — производный поисковый индекс; его можно пересобрать
`platform.search.reindex-on-startup=true`.

## 6. Конфигурация

| Префикс | Владелец | Пример |
|---|---|---|
| `platform.security.*` | starter | `jwt.secret`, `jwt.access-token-ttl`, `cors.allowed-origins` |
| `platform.storage.minio.*` | starter | `endpoint`, `public-endpoint`, `bucket-name` |
| `platform.search.*` | starter | `reindex-on-startup` |
| `platform.mail.*` | starter | SMTP |
| `security.service.*` | backend | `url`, `internal-token` |
| `security.internal.token` | security | проверка внутреннего вызова |
| `app.bootstrap.admin.*` | security | одноразовый администратор |

Production-профиль не содержит значений секретов: обязательные переменные
передаются из GitHub Environment/`.env` на сервере. Compose использует
`${VAR:?required}`, поэтому неполная конфигурация падает до запуска.

## 7. Отказоустойчивость

- MinIO/Elasticsearch/SMTP не обязательны для старта backend, если
  соответствующие флаги выключены.
- Недоступность `security-service` не мешает проверке JWT, но ломает
  обогащение пользовательских данных и операции, которым нужен профиль.
- `UserDirectory` возвращает контролируемые ошибки, а не stack trace.
- Оба сервиса имеют graceful shutdown и health endpoint
  `/api/v1/actuator/health`.

## 8. Связанные документы

- [DDD.md](DDD.md) — контексты, агрегаты, инварианты.
- [SECURITY.md](SECURITY.md) — секреты и модель угроз.
- [DEPLOYMENT.md](DEPLOYMENT.md) — CI/CD.
- [TESTING.md](TESTING.md) — тесты и smoke.
