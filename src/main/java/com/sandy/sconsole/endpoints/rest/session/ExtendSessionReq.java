package com.sandy.sconsole.endpoints.rest.session;

import lombok.Data;

import java.util.Date;

@Data
public class ExtendSessionReq {
    
    private int sessionId ;
    private int pauseId ;
    private int problemAttemptId ;
    
    private Date endTime ;
    
    private int sessionEffectiveDuration ;
    private int problemAttemptEffectiveDuration ;
}
