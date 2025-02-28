package com.sandy.sconsole.api.master.vo.reqres;

import lombok.Data;

import java.time.Instant;

@Data
public class ExtendSessionReq {
    
    private int sessionId ;
    private int pauseId ;
    private int problemAttemptId ;
    
    private Instant endTime ;
    
    private int sessionEffectiveDuration ;
    private int problemAttemptEffectiveDuration ;
}
