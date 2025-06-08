package com.sandy.sconsole.endpoints.websockets.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.dao.audit.repo.SessionEventRepo;
import com.sandy.sconsole.endpoints.websockets.monitor.payload.SessionEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@MessageMapping( "/app-monitor" )
public class AppMonitorWSController {
    
    public enum ResponseType { DAY_SESSION_EVENTS, SESSION_EVENT } ;
    
    @Autowired private SimpMessagingTemplate template ;
    @Autowired private SessionEventRepo sessionEventRepo ;
    
    private final MessageHeaders headers ;
    private final ObjectMapper mapper = new ObjectMapper() ;
    
    public AppMonitorWSController() {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create() ;
        accessor.setContentType( MimeTypeUtils.APPLICATION_JSON );
        headers = accessor.getMessageHeaders() ;
    }
    
    @MessageMapping( "/todaySessionEvents" )
    public void getAllStudyEvents() {
        
        List<SessionEventDTO> events = new ArrayList<>();
        sessionEventRepo.findEventsForToday().forEach( e -> {
            try {
                Object payload = mapper.readValue( e.getPayload(), HashMap.class ) ;
                events.add( new SessionEventDTO( e.getEventId(), e.getTime(), payload ) ) ;
            }
            catch( JsonProcessingException ex ) {
                log.error( "Error parsing session event payload: {}", e.getPayload(), ex );
            }
        } ) ;
        sendMessage( ResponseType.DAY_SESSION_EVENTS, events );
    }
    
    public void sendMessage( ResponseType resType, Object msg ) {
        
        AppMonitorResponse res = new AppMonitorResponse() ;
        res.setResponseType( resType.name() ) ;
        res.setPayload( msg ) ;
        
        this.template.convertAndSend( "/topic/app-monitor-responses", res, headers );
    }
}
