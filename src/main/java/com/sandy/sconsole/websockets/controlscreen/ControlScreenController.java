package com.sandy.sconsole.websockets.controlscreen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Date;

@Slf4j
@Controller
@MessageMapping( "/control-screen" )
public class ControlScreenController {
    
    @Autowired SimpMessagingTemplate template ;
    
    public ControlScreenController() {
    }

    @MessageMapping( "/current" )
    public void getCurrentScreenControlConfig() {
        log.debug( "Received a client request" ) ;
        this.template.convertAndSend( "/topic/control-screen-messages", new Date().toString() );
    }
}
