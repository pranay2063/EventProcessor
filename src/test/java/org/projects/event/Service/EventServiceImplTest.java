package org.projects.event.Service;

import org.junit.jupiter.api.Test;
import org.projects.event.DAO.Event;
import org.projects.event.DAO.EventDAO;
import org.projects.event.MQ.RabbitMqPublisher;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

class EventServiceImplTest {
    private EventServiceImpl service(EventDAO dao, RabbitMqPublisher publisher) throws Exception {
        EventServiceImpl service = new EventServiceImpl();
        set(service, "eventDAO", dao);
        set(service, "rabbitMqPublisher", publisher);
        return service;
    }

    private void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }

    @Test void acceptsExactlyValidRequiredFields() throws Exception {
        EventServiceImpl service = service(new EventDAO(), new RabbitMqPublisher());
        assertTrue(service.validateFields(Map.of("senderEmail", "a@example.com", "receiverEmail", "b@example.com", "message", "hello")));
        assertFalse(service.validateFields(Map.of("senderEmail", "a@example.com", "message", "hello")));
        assertFalse(service.validateFields(Map.of("senderEmail", "bad", "receiverEmail", "b@example.com", "message", "hello")));
        assertFalse(service.validateFields(Map.of("senderEmail", "a@example.com", "receiverEmail", "b@example.com", "message", " ")));
        assertFalse(service.validateFields(Map.of("senderEmail", "a@example.com", "receiverEmail", "b@example.com", "message", "hi", "extra", "x")));
    }

    @Test void returnsServerErrorWhenPublishFails() throws Exception {
        RabbitMqPublisher publisher = new RabbitMqPublisher() { @Override public void publish(String message) { throw new IllegalStateException("broker down"); } };
        var response = service(new EventDAO(), publisher).addEvent(new Event());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test void generatesUniqueUuidIds() throws Exception {
        EventServiceImpl service = service(new EventDAO(), new RabbitMqPublisher());
        String one = service.generateEventID(new Event());
        String two = service.generateEventID(new Event());
        assertNotEquals(one, two);
        assertDoesNotThrow(() -> java.util.UUID.fromString(one));
    }
}
