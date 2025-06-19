package com.sandy.sconsole.endpoints.websockets.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.bus.EventTargetMarker;
import com.sandy.sconsole.dao.audit.repo.SessionEventRepo;
import com.sandy.sconsole.endpoints.websockets.monitor.payload.DashboardState;
import com.sandy.sconsole.endpoints.websockets.monitor.payload.SessionEventDTO;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.sandy.sconsole.EventCatalog.ATS_MANAGER_REFRESHED;
import static com.sandy.sconsole.EventCatalog.ATS_REFRESHED;

@Slf4j
@Controller
@MessageMapping( "/app-monitor" )
public class AppMonitorWSController implements EventSubscriber {
    
    public enum ResponseType { DAY_SESSION_EVENTS, SESSION_EVENT, CURRENT_DASHBOARD_STATE }
    
    @Autowired private EventBus eventBus ;
    @Autowired private SimpMessagingTemplate template ;
    @Autowired private SessionEventRepo sessionEventRepo ;
    @Autowired private ActiveTopicStatisticsManager atsMgr ;
    @Autowired private TodaySessionStatistics todayStats ;
    
    private final MessageHeaders headers ;
    private final ObjectMapper mapper = new ObjectMapper() ;
    
    public AppMonitorWSController() {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create() ;
        accessor.setContentType( MimeTypeUtils.APPLICATION_JSON );
        headers = accessor.getMessageHeaders() ;
    }
    
    @PostConstruct
    public void init() {
        eventBus.addAsyncSubscriber( this, ATS_MANAGER_REFRESHED ) ;
        eventBus.addAsyncSubscriber( this, ATS_REFRESHED ) ;
    }
    
    @RequestMapping(value = { "/apps/jee/monitor/session-events", "/apps/jee/monitor/dashboard" })
    public String forwardRoutePathsToIndex() {
        return "forward:/apps/jee/monitor/index.html";
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
        sendMessage( ResponseType.DAY_SESSION_EVENTS, events ) ;
        sendMessage( ResponseType.CURRENT_DASHBOARD_STATE, new DashboardState( atsMgr, todayStats ) ) ;
    }
    
    public void sendMessage( ResponseType resType, Object msg ) {
        
        AppMonitorResponse res = new AppMonitorResponse() ;
        res.setResponseType( resType.name() ) ;
        res.setPayload( msg ) ;
        
        this.template.convertAndSend( "/topic/app-monitor-responses", res, headers );
    }
    
    @Override
    @EventTargetMarker( { ATS_MANAGER_REFRESHED, ATS_REFRESHED } )
    public void handleEvent( Event event ) {
        sendMessage( ResponseType.CURRENT_DASHBOARD_STATE, new DashboardState( atsMgr, todayStats ) ) ;
    }
}
