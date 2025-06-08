package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import lombok.Data;

import java.util.Date;

@Data
public class SessionStart {

    private int sessionId ;
    private Date startTime ;
    private String sessionType ;
    private String syllabusName ;
    private String topicName ;
}
