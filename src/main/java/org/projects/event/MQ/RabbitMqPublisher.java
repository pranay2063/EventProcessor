package org.projects.event.MQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;

@Component
public class RabbitMqPublisher implements Publisher {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqPublisher.class);
    private String exchangeName;
    private String routingKey;
    private ConnectionFactory connectionFactory;
    @Value("${spring.rabbitmq.host:localhost}") private String host;
    @Value("${spring.rabbitmq.port:5672}") private int port;
    @PostConstruct
    public void init(){
        logger.info("Reached publisher");
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        exchangeName = "events";
        routingKey = "eventkey";
    }

    @Override
    public void publish(String message) {
        try {
            try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchangeName, "topic", Boolean.TRUE);
            channel.confirmSelect();
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            if (!channel.waitForConfirms(5000)) throw new IllegalStateException("RabbitMQ did not confirm event publish");
            logger.info("Message {} published to exchange {} successfully", message, exchangeName);
            }
        }
        catch (Exception e){
            logger.error("Message {} publish failed with exception {}", message, e);
            throw new IllegalStateException("Unable to publish event to RabbitMQ", e);
        }
    }
}
