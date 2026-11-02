# DDD-модель CaseLab EDM

Документ фиксирует ubiquitous language, bounded contexts, агрегаты и правила
зависимостей. Он дополняет [ARCHITECTURE.md](ARCHITECTURE.md).

## 1. Bounded contexts

| Контекст | Тип | Код | Ключевые понятия |
|---|---|---|---|
| Security & Access | supporting | `security-service`, пакет `ru.caselab.edm.security` | User, Department, Role, RefreshToken, JWT |
| Document Management | core | `backend`, пакет `document` | Document, DocumentVersion, DocumentType, Attribute |
| Approval & Signing | core | `backend`, пакет `approval` | ApprovementProcess, ApprovementProcessItem, Signature, Vote |
| Delegation | supporting | `backend`, пакет `delegation` | ReplacementManager, делегирование подписи |
| Analytics | read model | `backend`, пакет `analytics` | агрегаты по документам, подписям, замещениям |
| Integration | infrastructure | `backend/integration`, `platform-spring-boot-starter` | ObjectStorage, EmailSender, Elasticsearch, JWT-фильтр |

Identity — отдельный сервис, а не модуль backend. Backend знает пользователей
только как UUID и `UserSummary`, полученный через `UserDirectory`.

## 2. Карта зависимостей

```text
security-service ──► platform-spring-boot-starter
       ▲
       │ HTTP /internal, X-Internal-Token
       │
backend ───────────► platform-spring-boot-starter
   │
   ├── document ◄── approval
   ├── document ◄── delegation
   └── analytics ──► repositories (read-only projections)

shared/directory ──► security-service (ACL)
shared/security  ──► resource-authorization contracts (внутри backend)
```

Правила:

1. `security-service` не зависит от `backend` и не знает о документах.
2. `backend` не импортирует identity-сущности и identity-репозитории.
3. Домен не зависит от адаптеров (JPA, MinIO, HTTP, mail).
4. API-слой не содержит бизнес-правил; контроллер вызывает application service.
5. Межконтекстные вызовы идут через application service или ACL, не через
   чужие репозитории напрямую.

## 3. Агрегаты и инварианты

### 3.1. Document

Агрегат: `Document` (корень) + `DocumentVersion` + значения атрибутов.

Инварианты:

- у документа есть владелец `ownerId` (UUID);
- версии принадлежат только одному документу;
- владелец — единственный, кто может менять/удалять документ и отправлять его
  на подписание;
- переходы состояния выполняет singleton `DocumentStateMachine`;
- последняя версия выбирается детерминированно: `createdAt`, затем `id`;
- файл версии хранится в MinIO, в БД — только имя объекта.

`DocumentType` и `Attribute` — отдельные справочники. Атрибуты типизированы
(`STRING`, `DATE`, `NUMBER` и т.д.), значения проверяются при создании версии.

### 3.2. Approval / Signing

Агрегат: `ApprovementProcess` + `ApprovementProcessItem`.

Инварианты:

- участники согласования уникальны в рамках версии;
- подписант хранится как `signerId` (UUID), без FK на identity;
- подписать заявку может только текущий подписант;
- повторная подпись той же заявки отклоняется `409`;
- хеш подписи считается по содержимому в UTF-8;
- голосование имеет дедлайн; сбор результатов идемпотентен.

`Signature` — отдельная сущность, связанная с item один-к-одному. Ограничение
уникальности закреплено и в БД (`uq_signatures_approvement_item`).

### 3.3. Delegation

Агрегат: `ReplacementManager`.

Инварианты:

- менеджер и временный менеджер — разные пользователи;
- период замещения имеет начало и конец;
- делегировать подпись можно только в активном периоде;
- целевой пользователь должен относиться к тому же подразделению, которым
  управляет владелец процесса;
- после делегирования `ApprovementProcessItem.signerId` меняется на временного
  менеджера.

Проверка подразделения выполняется через `UserDirectory`; backend не читает
identity-таблицы.

### 3.4. Identity

Агрегаты: `User`, `Department`, `Role`.

Инварианты:

- login и email уникальны;
- пароль хранится только в BCrypt-хеше;
- роли назначаются через `user_roles`;
- подразделение может иметь родителя, менеджеров и участников;
- refresh-токен привязан к пользователю и может быть отозван;
- bootstrap администратора выполняется один раз и только при явном флаге.

## 4. Read models

`analytics` не является транзакционным контекстом. Запросы возвращают
проекции/агрегаты и не меняют доменные сущности. Для отображения логинов
backend пакетно запрашивает `UserDirectory.findUsers(...)`; отсутствие
пользователя не должно ломать отчёт.

## 5. События и планировщик

- Уведомления отправляются через `EmailSender`; SMTP-ошибка не откатывает
  бизнес-транзакцию.
- Планировщик голосования завершает процесс по дедлайну; повторный запуск не
  создаёт дублей.
- Elasticsearch-индекс атрибутов — производный; переиндексация опциональна и
  не влияет на источник истины.

## 6. Технические границы

- JPA-сущности не используют `@Data`, `equals/hashCode` — по id.
- `spring.jpa.open-in-view=false`: выборки и транзакции должны быть явными.
- UUID-ссылки на identity не являются JPA-ассоциациями.
- Внутренние HTTP-контракты (`/internal/**`) не считаются публичным API и могут
  меняться вместе с обоими сервисами.
