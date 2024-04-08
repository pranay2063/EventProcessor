# EventProcessor

Event Processor exposes an api to send events via email and read all sent mails per user.

![EventProcessor](https://github.com/pranay2063/EventProcessor/assets/7464990/fd0c0edd-8acc-44f7-8f1c-8cfc9f062117)

**Web links**
1. [Swagger UI](http://localhost:8080/)
2. [RabbitMQ Management UI](http://localhost:15672/)

**How to run the app locally**
1. Start rabbitMQ from docker-compose
2. Create exchange (events), queue (event) and bind them together with binding key (eventkey)
3. Pull latest app image using the command: docker-compose build --pull
4. Start the app from docker-compose 
