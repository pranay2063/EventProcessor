package org.projects.event.MQ;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RabbitMqPublisherTest {
    @Test void propagatesBrokerConnectionFailureToCaller() throws Exception {
        RabbitMqPublisher publisher = new RabbitMqPublisher();
        publisher.init();
        ConnectionFactory factory = new ConnectionFactory() {
            @Override public com.rabbitmq.client.Connection newConnection() throws java.io.IOException { throw new java.io.IOException("offline"); }
        };
        Field field = RabbitMqPublisher.class.getDeclaredField("connectionFactory"); field.setAccessible(true); field.set(publisher, factory);
        assertThrows(IllegalStateException.class, () -> publisher.publish("{}"));
    }
}
