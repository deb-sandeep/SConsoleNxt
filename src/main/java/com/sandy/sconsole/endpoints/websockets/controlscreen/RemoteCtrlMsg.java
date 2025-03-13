package com.sandy.sconsole.endpoints.websockets.controlscreen;

import lombok.Data;

import java.util.Collection;

@Data
public class RemoteCtrlMsg {
    
    public enum MessageType {
        SHOW_SCREEN,
        LIFESPAN_COUNTER,
    }

    private MessageType        messageType ;
    private String             screenId ;
    private String             screenName;
    private Collection<String> possibleNextScreens ;
    private int                remainingLifespan ;
    
    public RemoteCtrlMsg( MessageType messageType ) {
        this.messageType = messageType ;
    }
}
