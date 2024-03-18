package org.projects.event.DAO;

import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EventDAO {

    List<Event> events;
    Map<String, List<Event>> eventsPerUser;

    @PostConstruct
    public void init(){
        events = new ArrayList<>();
        eventsPerUser = new HashMap<>();
    }

    public ResponseEntity<Object> addEvent(Event event){
        try {
            events.add(event);
            List<Event> currentData;
            String sender = event.getSenderEmail();
            if(eventsPerUser.containsKey(sender)){
                currentData = eventsPerUser.get(sender);
            }
            else {
                currentData = new ArrayList<>();
            }
            currentData.add(event);
            eventsPerUser.put(sender, currentData);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("Event added successfully");
    }

    public List<Event> queryEvent(String name){
        if(eventsPerUser.containsKey(name)){
            return eventsPerUser.get(name);
        }
        return new ArrayList<>();
    }

}
