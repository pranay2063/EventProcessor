package org.projects.event.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.projects.event.DAO.Event;
import org.projects.event.Service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @Operation(summary = "email event from sender email id to receiver email id with a message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "events", method = RequestMethod.POST)
    public ResponseEntity<Object> addEvent(@RequestBody @Schema(implementation = Event.class) Map<String, String> parameters) throws JsonProcessingException {
        if(CollectionUtils.isEmpty(parameters)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payload is empty");
        }
        if(!eventService.validateFields(parameters)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid field(s)");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Event event = objectMapper.convertValue(parameters, Event.class);
        event.setId(eventService.generateEventID(event));
        return eventService.addEvent(event);
    }

    @Operation(summary = "fetch events sent by a user/sender by email id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "events", method = RequestMethod.GET)
    public ResponseEntity<Object> getEvent(@RequestParam(name = "Email") String user){
        if(!StringUtils.hasLength(user)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User name is empty");
        }
        List<Event> response = eventService.getEvent(user);
        if(!CollectionUtils.isEmpty(response)){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body("No email found for the user "+user);
    }

}
