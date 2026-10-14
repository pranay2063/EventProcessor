package org.projects.event.Controller;

import org.junit.jupiter.api.Test;
import org.projects.event.Service.EventService;
import org.springframework.http.HttpStatus;
import org.projects.event.DAO.Event;
import org.springframework.http.ResponseEntity;
import java.lang.reflect.Field;
import java.util.List;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EventControllerTest {
    @Test void rejectsEmptyAndInvalidPayloads() throws Exception {
        EventService service = service(false);
        EventController controller = new EventController();
        set(controller, service);
        assertEquals(HttpStatus.BAD_REQUEST, controller.addEvent(Map.of()).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.addEvent(Map.of("oops", "x")).getStatusCode());
    }

    @Test void acceptsValidPayloadAndUsesLowercaseEmailQueryParameter() throws Exception {
        EventService service = service(true);
        EventController controller = new EventController();
        set(controller, service);
        assertEquals(HttpStatus.OK, controller.addEvent(Map.of("senderEmail", "a@example.com", "receiverEmail", "b@example.com", "message", "hi")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getEvent("a@example.com").getStatusCode());
    }

    private EventService service(boolean valid) {
        return new EventService() {
            public ResponseEntity<Object> addEvent(Event event) { return ResponseEntity.ok("accepted"); }
            public List<Event> getEvent(String name) { return List.of(); }
            public Boolean validateFields(Map<String, String> parameters) { return valid; }
            public String generateEventID(Event event) { return "generated-id"; }
        };
    }
    private void set(EventController controller, EventService service) throws Exception {
        Field field = EventController.class.getDeclaredField("eventService"); field.setAccessible(true); field.set(controller, service);
    }
}
