package com.sandy.sconsole.endpoints.websockets;

import com.sandy.sconsole.endpoints.websockets.monitor.AppMonitorWSController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
public class WebSocketSubscriptionListener {
    
    @Autowired private AppMonitorWSController appMonitorWSController ;
    
    @EventListener
    public void handleSubscription( SessionSubscribeEvent event) {
        
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = accessor.getSessionId() ;
        String destination = accessor.getDestination() ;
        if( destination!= null && destination.equals( "/topic/app-monitor-responses" ) ) {
            appMonitorWSController.getAllStudyEvents() ;
            log.debug( "Received subscription request for /topic/app-monitor-responses from session: {}", sessionId ) ;
            log.debug( "  Sending all study events to session: {}", sessionId ) ;
        }
    }
}
