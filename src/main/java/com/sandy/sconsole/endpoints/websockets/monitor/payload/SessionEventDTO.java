package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import lombok.Data;

import java.util.Date;

@Data
public class SessionEventDTO {
    private String eventType ;
    private Date   time ;
    private Object payload ;
    
    public SessionEventDTO( String eventType, Date time, Object payload ) {
        this.eventType = eventType ;
        this.time = time ;
        this.payload = payload ;
    }
}
