package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import lombok.Data;

import java.util.Date;

@Data
public class PauseEnd {

    private int sessionId ;
    private int pauseId ;
    private Date startTime ;
    private Date endTime ;
    private int  duration ;
}
