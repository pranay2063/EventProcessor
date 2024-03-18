package org.projects.event.MQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class RabbitMqPublisher implements Publisher {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqPublisher.class);
    private String exchangeName;
    private String routingKey;
    private ConnectionFactory connectionFactory;
    @PostConstruct
    public void init(){
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        exchangeName = "events";
        routingKey = "eventkey";
    }

    @Override
    public void publish(String message) {
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, "topic", Boolean.TRUE);
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            logger.info("Message {} published to exchange {} successfully", message, exchangeName);
        }
        catch (Exception e){
            logger.error("Message {} publish failed with exception {}", message, e);
        }
    }
}
