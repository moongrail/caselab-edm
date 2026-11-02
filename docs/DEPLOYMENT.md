# Деплой и окружения

## 1. Модель

- `dev` — интеграционная ветка. Push и PR запускают тесты, деплоя нет.
- `main` — production-ветка. Merge/push в `main` запускает полный конвейер:
  тесты → сборка и публикация двух образов → автоматический деплой →
  health-check обоих сервисов.
- Ручной запуск production Compose не является штатным способом выпуска.
  Штатный путь — merge в `main`.

## 2. GitHub Actions

Файлы:

- `.github/workflows/main-workflow.yml` — точки входа и порядок jobs;
- `reusable-test.yml` — JDK 17, Maven Wrapper, `clean verify` всего reactor,
  JaCoCo для трёх модулей и публикация отчётов;
- `reusable-build.yml` — multi-stage Docker build, push в Docker Hub; один
  вызов собирает backend, второй — security-service;
- `reusable-deploy.yml` — копирование `docker-compose-server.yaml`, pull/up,
  health-check.

Деплой запускается только для `refs/heads/main` после успешных `test`,
`build-backend` и `build-security`. Concurrency запрещает одновременные
деплои. Оба образа собираются из корня репозитория, поэтому изменения starter
проходят один и тот же конвейер.

## 3. Необходимые GitHub Secrets

Настроить в репозитории и/или Environment `prod`:

| Secret | Назначение |
|---|---|
| `DOCKER_USERNAME` | логин Docker Hub |
| `DOCKER_TOKEN` | токен с правом push |
| `SERVER_HOST` | хост/домен сервера (только в секрете) |
| `SERVER_USERNAME` | deploy-пользователь |
| `SERVER_KEY` | приватный SSH-ключ (PEM/OpenSSH) |
| `SERVER_PORT` | SSH-порт |

Environment `prod` рекомендуется защитить: required reviewers, запрет
самостоятельного approve, ограничение ветки `main`.

## 4. Одноразовая настройка сервера

1. Установить Docker Engine и Compose plugin.
2. Создать непривилегированного пользователя `deploy`, добавить его в группу
   `docker` (или настроить sudo только для нужных compose-команд).
3. Добавить публичный SSH-ключ в `~deploy/.ssh/authorized_keys`, отключить
   парольный вход и root-логин.
4. Создать каталог деплоя `/home/ubuntu` (используется workflow) или изменить
   `scp_remote` в `main-workflow.yml`.
5. Создать `/home/ubuntu/.env` на основе `.env.example`, заполнить реальными
   значениями, выставить `chmod 600 .env`.
6. Настроить TLS/reverse proxy: наружу публикуются API, но **не**
   `/internal/**`.
7. Открыть firewall для SSH и HTTPS, остальное закрыть.
8. Настроить `docker login` на сервере, если образы приватные.
9. Выполнить первый merge в `main`; дальше деплой выполняется автоматически.

`scp-action` копирует `docker/docker-compose-server.yaml` в `/home/ubuntu` с
`strip_components: 1`. Compose-файл лежит рядом с `.env`; `.env` workflow не
перезаписывает.

## 5. Переменные production

Обязательные переменные `docker/docker-compose-server.yaml`:

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` — база backend;
- `SECURITY_POSTGRES_DB`, `SECURITY_POSTGRES_USER`,
  `SECURITY_POSTGRES_PASSWORD` — отдельная база identity;
- `BACKEND_IMAGE`, `SECURITY_IMAGE` — опубликованные образы;
- `JWT_SECRET` — одинаковый для backend и security;
- `SECURITY_INTERNAL_TOKEN` — секрет внутреннего directory API;
- `CORS_ALLOWED_ORIGINS`;
- `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_PUBLIC_ENDPOINT`;
- `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD` — SMTP обязателен в production;
- `OPENAPI_SERVER_URL`.

Необязательные: `JWT_ACCESS_TOKEN_TTL`, `JWT_REFRESH_TOKEN_TTL`, `MAIL_PORT`,
`MINIO_BUCKET`, `ELASTIC_REINDEX_ON_STARTUP`, `BOOTSTRAP_ADMIN_*`.

`MINIO_ENDPOINT` и `ELASTIC_ENDPOINT` задаются внутри compose как внутренние
адреса контейнеров. Backend получает `SECURITY_SERVICE_URL` также из compose.
`SPRING_PROFILES_ACTIVE=prod` задаётся для backend.

Без обязательной переменной Compose завершится с ошибкой `... is required` —
это защита от запуска с неполной конфигурацией.

## 6. Первый запуск и миграции

1. `security-service` стартует, Liquibase применяет identity-схему.
2. Backend стартует после health identity-сервиса, Liquibase применяет схему
   документооборота.
3. Hibernate проверяет обе схемы (`ddl-auto=validate`) и падает при
   расхождении.
4. Если база identity пустая, временно включить `BOOTSTRAP_ADMIN_ENABLED=true`
   и задать `BOOTSTRAP_ADMIN_*`.
5. После создания администратора выключить bootstrap и перезапустить
   `security-service`.
6. Проверить оба health endpoint и Swagger.

Существующие базы перед первым деплоем новой версии необходимо забэкапить.

Для backend-базы Liquibase 1.2 автоматически обрабатывает старую схему:

- переименовывает `documents.user_id` в `owner_id`;
- переименовывает `approvment_process_item.user_id` в `signer_id`;
- удаляет старые FK на `users` у документов, заявок и
  `replacement_managers`;
- сохраняет UUID существующих владельцев и подписантов.

Старые identity-таблицы (`users`, `roles`, `refresh_token` и т.д.) могут
остаться в backend-базе, но приложение их больше не читает. Перенос
пользователей в отдельную базу `security` и запуск `security-service` на этой
схеме выполняются отдельно: нужна копия identity-таблиц либо чистый запуск с
bootstrap администратора. Перед переключением проверьте UUID-ссылки и
ротируйте/удалите демо-учётки.

## 7. Health-check и диагностика

Workflow после `up -d` опрашивает:

```text
GET http://localhost:8081/api/v1/actuator/health
GET http://localhost:8080/api/v1/actuator/health
```

до 30 попыток с интервалом 5 секунд. При неудаче печатаются `compose ps` и
последние 200 строк логов `security` и `backend`, job завершается ошибкой.

Типовые причины:

| Симптом | Проверить |
|---|---|
| security не стартует | `SECURITY_DATABASE_URL`, `JWT_SECRET`, миграции identity |
| backend не стартует | обязательные переменные `.env`, `docker compose config` |
| `Schema-validation` | неприменённая миграция, несоответствие сущности и БД |
| 401 на directory API | одинаковый `SECURITY_INTERNAL_TOKEN` в двух сервисах |
| 401 на health | неверный контекстный путь/прокси |
| health DOWN | подключение к БД, MinIO, Elasticsearch |
| образ не скачивается | Docker Hub token/приватность образа |

## 8. Откат

Штатный откат — revert проблемного коммита в `main`. Это запускает тот же
автоматический конвейер и возвращает предыдущее состояние кода и схемы (если
миграции обратимы).

Правила:

- миграции должны быть обратно совместимыми: сначала добавить новое, потом
  переключить код, затем удалять старое;
- перед рискованными изменениями — backup обеих БД;
- не выполнять ручные `docker run`/`compose up` как основной способ отката;
  аварийные действия допустимы только по runbook и с последующей нормализацией
  через `main`.

## 9. Обновление секретов

1. Обновить значение в GitHub Environment/Secrets и/или в `.env` на сервере.
2. Для `JWT_SECRET` — обновить оба сервиса; старые access-токены станут
   невалидными.
3. Для `SECURITY_INTERNAL_TOKEN` — обновить security и backend одновременно.
4. Для БД/MinIO/SMTP — сначала подготовить новый доступ, затем переключить
   приложение, затем отозвать старый.
5. Проверить health, логи и ключевые сценарии.
6. Зафиксировать дату ротации во внутреннем журнале (вне репозитория).

Подробнее: [SECURITY.md](SECURITY.md).

## 10. Локальная проверка конфигурации

Перед merge полезно проверить синтаксис compose:

```bash
docker compose --env-file .env -f docker/docker-compose-server.yaml config
```

Это не деплой и не заменяет CI. Полный стек локально:
`docker/docker-compose-all.yaml`; см. [README](../README.md).
