# EventProcessor

Event Processor exposes an api to send events via email and read all sent mails per user.

![EventProcessor](https://github.com/pranay2063/EventProcessor/assets/7464990/fd0c0edd-8acc-44f7-8f1c-8cfc9f062117)

**Web links**
[Swagger UI](http://localhost:8080/)
[RabbitMQ Management UI](http://localhost:15672/)

**How to run the app locally**
Start rabbitMQ from docker-compose
Create exchange (events), queue (event) and bind them together with binding key (eventkey)
Pull latest app image using the command: docker-compose build --pull
Start the app from docker-compose 
