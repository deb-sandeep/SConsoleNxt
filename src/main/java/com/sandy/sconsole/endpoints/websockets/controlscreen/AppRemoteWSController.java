package com.sandy.sconsole.endpoints.websockets.controlscreen;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;

import static com.sandy.sconsole.endpoints.websockets.controlscreen.RemoteCtrlMsg.MessageType.LIFESPAN_COUNTER;
import static com.sandy.sconsole.endpoints.websockets.controlscreen.RemoteCtrlMsg.MessageType.SHOW_SCREEN;

@Slf4j
@Controller
@MessageMapping( "/app-remote" )
public class AppRemoteWSController {
    
    @Autowired SimpMessagingTemplate template ;
    @Autowired ScreenManager screenManager ;
    
    private final MessageHeaders headers ;
    
    public AppRemoteWSController() {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create() ;
        accessor.setContentType( MimeTypeUtils.APPLICATION_JSON );
        headers = accessor.getMessageHeaders() ;
    }
    
    @MessageMapping( "/showScreen" )
    public void showScreen( String screenId ) {
        screenManager.scheduleScreenChange( screenId ) ;
    }
    
    // Sends a peer screen display message to the app remote for the active screen
    @MessageMapping( "/currentScreen" )
    public void sendPeerScreenDisplayMsg() {
        Screen currentScreen = screenManager.getCurrentScreen() ;
        
        if( currentScreen != null ) {
            RemoteCtrlMsg msg = new RemoteCtrlMsg( SHOW_SCREEN );
            msg.setScreenId( currentScreen.getId() );
            msg.setScreenName( currentScreen.getScreenName() );
            msg.setPossibleNextScreens( screenManager.getScreenTransitions( currentScreen.getId() ) );
            
            sendMessage( msg ) ;
        }
    }
    
    public void sendScreenTimeLeft( int timeLeft ) {
        
        Screen currentScreen = screenManager.getCurrentScreen() ;

        RemoteCtrlMsg msg = new RemoteCtrlMsg( LIFESPAN_COUNTER ) ;
        msg.setScreenId( currentScreen.getId() ) ;
        msg.setRemainingLifespan( timeLeft ) ;
        
        sendMessage( msg ) ;
    }
    
    private void sendMessage( RemoteCtrlMsg msg ) {
        this.template.convertAndSend( "/topic/remote-screen-messages", msg, headers );
    }
}
