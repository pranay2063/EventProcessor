package org.projects.event.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.projects.event.DAO.Event;
import org.projects.event.DAO.EventDAO;
import org.projects.event.MQ.RabbitMqPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EventServiceImpl implements EventService{

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private static AtomicInteger sequenceNumber = new AtomicInteger();

    @Autowired
    private EventDAO eventDAO;

    @Autowired
    private RabbitMqPublisher rabbitMqPublisher;

    @Override
    public ResponseEntity<Object> addEvent(Event event) throws JsonProcessingException {
        try {
            rabbitMqPublisher.publish(new ObjectMapper().writeValueAsString(event));
        }
        catch (Exception e){
            logger.error("Exception {} while publishing the record {} in rabbitmq", e, event);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("Event added successfully: "+new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(event));
    }

    @Override
    public List<Event> getEvent(String name) {
        return eventDAO.queryEvent(name);
    }

    @Override
    public Boolean validateFields(Map<String, String> parameters) {
        for(Map.Entry<String, String> entry: parameters.entrySet()){
            switch (entry.getKey()){
                case "senderEmail":
                case "message":
                case "receiverEmail":
                    break;
                default:
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public String generateEventID(Event event) {
        return Math.abs(event.getSenderEmail().hashCode()) + "-" + System.currentTimeMillis() + "-" + sequenceNumber.incrementAndGet();
    }
}
