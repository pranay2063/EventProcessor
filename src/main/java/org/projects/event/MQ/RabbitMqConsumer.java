package org.projects.event.MQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import jakarta.annotation.PostConstruct;
import org.projects.event.DAO.Event;
import org.projects.event.DAO.EventDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitMqConsumer implements BasicConsumer {

    private final static Logger logger = LoggerFactory.getLogger(RabbitMqConsumer.class);
    private ConnectionFactory connectionFactory;
    private String queue;
    private Connection connection;
    private Channel channel;

    private Boolean autoAcknowledgment;
    private int prefetchCount;
    private Map<String, Object> queueArguments;
    private DeliverCallback deliverCallback;
    private Consumer consumer;
    @Autowired
    private EventDAO eventDAO;

    @PostConstruct
    public void init() throws IOException, TimeoutException {
        logger.info("Reached consumer init");
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("host.docker.internal");
        connectionFactory.setPort(5672);
        queue = "event";
        autoAcknowledgment = Boolean.FALSE;
        prefetchCount = 1;
        queueArguments = new HashMap<>();
        queueArguments.put("x-queue-type", "quorum");
        queueArguments.put("x-queue-mode", "lazy");
        startConsumer();
    }

    public void startConsumer() throws IOException, TimeoutException {
        logger.info("Reached consumer startConsumer");
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(queue, true, false, false, queueArguments);
        deliverCallback = (consumerTag, delivery) -> {
            Event event = new ObjectMapper().readValue(delivery.getBody(), Event.class);
            eventDAO.addEvent(event);
            if(!autoAcknowledgment){
                // https://www.rabbitmq.com/docs/confirms#acknowledgement-modes
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
            logger.info("Message consumed {}", event);
        };
        channel.addShutdownListener(listener -> {
            if(!listener.isInitiatedByApplication()){
                logger.info("Unexpected channel shutdown, attempting to recover");
                try {
                    startConsumer();
                } catch (IOException | TimeoutException e) {
                    logger.error("Channel recovery failed with exception", e);
                }
            }
        });
        // https://www.rabbitmq.com/docs/consumer-prefetch
        channel.basicQos(prefetchCount, false);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(this::consume);
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
}
