package org.projects.event.Service;

import org.projects.event.DAO.Event;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface EventService {

    ResponseEntity<Object> addEvent(Event event);
    List<Event> getEvent(String name);

    Boolean validateFields(Map<String, String> parameters);

}
