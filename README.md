### Temp описание, потом распишу лучше.

### Запуск
- cd docker, docker compose -f docker-compose-db.yaml up -d для поднятия базы, минио. в общем пакете лежит сборщик бекэнд+бд.

### API
- ко всем эндпоинтам приписывается api/v1
  т.е. если вы пишите @ReqMap("/test") будет api/v1/test

### Правила коммитов в ветку
- отбиваемся всегда из dev, если ваша задача это подзадача другой фьючи, то отбивайтесь из другой фьючи.
- нейминг feature/КРАТКО-ЗАДАЧУ_описание
- пул реквесты скидывайте в чат 2 человека на аппрув пул реквеста.
-  -c feat:,fix:,style:
### Правила Liquibase
Есть главный файл ликвибейз(db.master.xml) в нем содержаться все

прошлые мастер скрипты изменений базы, например 1.0 в есть свой мастер скрипт

а вот он уже содержит конкретные sql скрипты. пример оформления в db.1.0-test_script.xml

### Swagger
http://localhost:8080/api/v1/swagger-ui/index.html

### TEST. пишите тесты плиз. Они вам сохранят время

### Docker hub
#### Сделать тег(Это примеры, скорее всего понадобится менять только бекэнд приложения)
docker tag postgres:14-alpine ваш_аккаунт_докер/caselab-edm-db:версия(пример 0.0.1)

docker tag minio/minio:RELEASE.2024-10-13T13-34-11Z.fips ваш_аккаунт_докер/caselab-minio::версия(пример 0.0.1)
#### Сделать пуш на репозиторий докер хаба
docker push ваш_аккаунт_докер/edm-db:версия(пример 0.0.1)

docker push ваш_аккаунт_докер/minio:версия(пример 0.0.1)
#### Если понадобится сделать хотфикс на вируталке заливайте  в докер хаб и поднимайте на сервере
#### Поднять приложение
sudo docker run -d --net=host --restart unless-stopped \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=postgres \
-e POSTGRES_DB=edm \
moongrail/caselab-edm-db:0.0.1

sudo docker run -d --name minio \
--restart unless-stopped --net=host \
-e MINIO_ROOT_USER=minio-user \
-e MINIO_ROOT_PASSWORD=minio-password \
moongrail/caselab-minio:0.0.1 server /data --console-address :9090

sudo docker run -d --net=host --restart unless-stopped --name backend-container \
-p 8080:8080 \
-e DATABASE_URL=jdbc:postgresql://localhost:5432/edm \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=postgres \
-e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
-e MINIO_ROOT_USER=minio-user \
-e MINIO_ROOT_PASSWORD=minio-password \
-e MINIO_ENDPOINT=http://localhost:9000 \
moongrail/caselab-backend:0.0.1

#### Другие команды
sudo systemctl start docker (Если вдруг не будет включён( поставил на авт включение))вщ
sudo systemctl status docker