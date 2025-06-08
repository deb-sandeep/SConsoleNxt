package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import lombok.Data;

import java.util.Date;

@Data
public class SessionEnd {

    private int sessionId ;
    private Date startTime ;
    private Date endTime ;
    private int effectiveDuration ;
    private String sessionType ;
    private String syllabusName ;
    private String topicName ;
}
