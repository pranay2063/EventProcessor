package org.projects.event.DAO;

import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class EventDAO {

    private ConcurrentMap<String, List<Event>> eventsPerUser;

    @PostConstruct
    public void init(){
        eventsPerUser = new ConcurrentHashMap<>();
    }

    public ResponseEntity<Object> addEvent(Event event){
        try {
            String sender = event.getSenderEmail();
            eventsPerUser.computeIfAbsent(sender, key -> new CopyOnWriteArrayList<>()).add(event);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("Event added successfully");
    }

    public List<Event> queryEvent(String name){
        List<Event> found = eventsPerUser.get(name);
        return found == null ? java.util.Collections.emptyList() : List.copyOf(found);
    }

}
