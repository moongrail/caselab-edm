### Temp описание, потом распишу лучше.

### API
- ко всем эндпоинтам приписывается api/v1
т.е. если вы пишите @ReqMap("/test") будет api/v1/test
### База BD
- на порту 5435 из докера

### Правила коммитов в ветку
- отбиваемся всегда из dev (и пул реквесты в неё создаём.)
- нейминг feature/КРАТКО-ЗАДАЧУ_описание
- пул реквесты скидывайте в чат 1 будет для мержа нужен( =))))) )
### Правила Liquibase
Есть главный файл ликвибейз(db.master.xml) в нем содержаться все 

прошлые мастер скрипты изменений базы, например 1.0 в есть свой мастер скрипт

а вот он уже содержит конкретные sql скрипты. пример оформления в db.1.0-test_script.xml

### Swagger
http://localhost:8080/api/v1/swagger-ui/index.html