package org.projects.event.MQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.projects.event.DAO.Event;
import org.projects.event.DAO.EventDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitMqConsumer implements BasicConsumer {

    private final static Logger logger = LoggerFactory.getLogger(RabbitMqConsumer.class);
    private ConnectionFactory connectionFactory;
    private String queue;
    private String exchangeName;
    private String routingKey;
    private Connection connection;
    private Channel channel;

    private Boolean autoAcknowledgment;
    private int prefetchCount;
    private Map<String, Object> queueArguments;
    private DeliverCallback deliverCallback;
    private Consumer consumer;
    @org.springframework.beans.factory.annotation.Value("${spring.rabbitmq.host:localhost}") private String host;
    @org.springframework.beans.factory.annotation.Value("${spring.rabbitmq.port:5672}") private int port;
    @Autowired
    private EventDAO eventDAO;

    @PostConstruct
    public void init() throws IOException, TimeoutException {
        logger.info("Reached consumer init");
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        queue = "event";
        exchangeName = "events";
        routingKey = "eventkey";
        autoAcknowledgment = Boolean.FALSE;
        prefetchCount = 1;
        queueArguments = new HashMap<>();
        queueArguments.put("x-queue-type", "quorum");
        startConsumer();
    }

    public void startConsumer() throws IOException, TimeoutException {
        logger.info("Reached consumer startConsumer");
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, "topic", true);
        channel.queueDeclare(queue, true, false, false, queueArguments);
        channel.queueBind(queue, exchangeName, routingKey);
        deliverCallback = (consumerTag, delivery) -> {
            try {
                Event event = new ObjectMapper().readValue(delivery.getBody(), Event.class);
                org.springframework.http.ResponseEntity<Object> result = eventDAO.addEvent(event);
                if (!result.getStatusCode().is2xxSuccessful()) throw new IllegalStateException("Event persistence failed: " + result.getBody());
                if (!autoAcknowledgment) channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                logger.info("Message consumed {}", event);
            } catch (Exception e) {
                logger.error("Event consumption failed; returning message to queue", e);
                if (!autoAcknowledgment && channel.isOpen()) channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };
        // https://www.rabbitmq.com/docs/consumer-prefetch
        channel.basicQos(prefetchCount, false);
        consume();
    }

    @Override
    public void consume() {
        try {
            channel.basicConsume(queue, autoAcknowledgment, deliverCallback, consumerTag -> {});
        }
        catch (Exception e){
            logger.error("Consumption failed with exception", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try { if (channel != null && channel.isOpen()) channel.close(); }
        catch (Exception e) { logger.warn("Failed to close RabbitMQ channel", e); }
        try { if (connection != null && connection.isOpen()) connection.close(); }
        catch (Exception e) { logger.warn("Failed to close RabbitMQ connection", e); }
    }
}
