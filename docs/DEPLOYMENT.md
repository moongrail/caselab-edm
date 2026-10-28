# Деплой и окружения

## 1. Модель

- `dev` — интеграционная ветка, каждый push запускает тесты.
- PR в `dev` — тесты обязательны, деплоя нет.
- `main` — production-ветка. Push/merge в `main` запускает полный конвейер:
  тесты → сборка и публикация образа → деплой на сервер → health-check.

Ручной запуск Docker Compose на сервере **не является** штатным способом выпуска.
Штатный путь — merge в `main`.

## 2. GitHub Actions

Файлы:

- `.github/workflows/main-workflow.yml` — точки входа и порядок jobs;
- `reusable-test.yml` — JDK 17, Maven Wrapper, `clean verify`, JaCoCo, отчёты;
- `reusable-build.yml` — multi-stage Docker build, push в Docker Hub;
- `reusable-deploy.yml` — копирование compose на сервер, pull/up, health-check.

Деплой запускается только для `refs/heads/main` и только после успешных `test` и
`build`. Concurrency не позволяет двум деплоям выполняться одновременно.

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
6. Убедиться, что порты 8080/9000 доступны только так, как требует архитектура;
   TLS и внешний доступ — через reverse proxy.
7. Открыть firewall для SSH и HTTPS, остальное закрыть.
8. Настроить `docker login` на сервере, если образ приватный.
9. Выполнить первый деплой merge в `main`; дальше — только автоматически.

`scp-action` копирует `docker/docker-compose-server.yaml` в `/home/ubuntu` с
`strip_components: 1`. В результате на сервере лежит `docker-compose-server.yaml`
рядом с `.env`.

## 5. Переменные production

Обязательные переменные `docker/docker-compose-server.yaml`:

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`;
- `JWT_SECRET`;
- `CORS_ALLOWED_ORIGINS`;
- `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_PUBLIC_ENDPOINT`;
- `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD` — SMTP обязателен в production;
- `OPENAPI_SERVER_URL`.

Необязательные (имеют безопасные значения по умолчанию): `JWT_ACCESS_TOKEN_TTL`,
`JWT_REFRESH_TOKEN_TTL`, `MAIL_PORT`, `MINIO_BUCKET`,
`ELASTIC_REINDEX_ON_STARTUP`, `BOOTSTRAP_ADMIN_*`.

`MINIO_ENDPOINT` и `ELASTIC_ENDPOINT` задаются внутри compose как внутренние адреса
контейнеров. `SPRING_PROFILES_ACTIVE=prod`.

Без обязательной переменной Compose завершится с ошибкой `... is required` — это
защита от запуска с неполной конфигурацией.

## 6. Первый запуск и миграции

1. Backend стартует, Liquibase применяет changelog.
2. Hibernate проверяет схему (`ddl-auto=validate`) и падает при расхождении.
3. Если БД пустая и нужен администратор, временно включить
   `BOOTSTRAP_ADMIN_ENABLED=true` и задать `BOOTSTRAP_ADMIN_*`.
4. После создания администратора выключить bootstrap и перезапустить сервис.
5. Проверить `/api/v1/actuator/health` и Swagger.

Существующую БД перед первым деплоем новой версии необходимо забэкапить. Миграции
не удаляют пользовательские данные; старые демо-учётки могут остаться и должны быть
удалены/ротированы вручную.

## 7. Health-check и диагностика

Workflow после `up -d` опрашивает:

```text
GET http://localhost:8080/api/v1/actuator/health
```

до 30 попыток с интервалом 5 секунд. При неудаче печатаются `compose ps` и
последние 200 строк логов backend, job завершается ошибкой.

Типовые причины:

| Симптом | Проверить |
|---|---|
| backend не стартует | обязательные переменные `.env`, `docker compose config` |
| `Schema-validation` | неприменённая миграция, несоответствие сущности и БД |
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
- перед рискованными изменениями — backup БД;
- не выполнять ручные `docker run`/`compose up` как основной способ отката;
  аварийные действия допустимы только по runbook и с последующей нормализацией
  через `main`.

## 9. Обновление секретов

1. Обновить значение в GitHub Environment/Secrets и/или в `.env` на сервере.
2. Для JWT — перезапустить backend (старые токены инвалидируются).
3. Для БД/MinIO/SMTP — сначала подготовить новый доступ, затем переключить
   приложение, затем отозвать старый.
4. Проверить health, логи и ключевые сценарии.
5. Зафиксировать дату ротации в внутреннем журнале (вне репозитория).

Подробнее: [SECURITY.md](SECURITY.md).

## 10. Локальная проверка конфигурации

Перед merge полезно проверить, что compose синтаксически валиден:

```bash
docker compose --env-file .env -f docker/docker-compose-server.yaml config
```

Это не деплой и не заменяет CI. Полный стек локально:
`docker/docker-compose-all.yaml`; см. [README](../README.md).
