package com.sandy.sconsole.dao.session.dto;

import com.sandy.sconsole.dao.session.ProblemAttempt;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class ProblemAttemptDTO implements Serializable {
    private Integer id;
    private Integer sessionId;
    private Integer problemId;
    private Instant startTime;
    private Instant endTime;
    private Integer effectiveDuration;
    private String  prevState;
    private String  targetState;
    
    public ProblemAttemptDTO() {}
    
    public ProblemAttemptDTO( ProblemAttempt pa ) {
        this.id = pa.getId() ;
        this.sessionId = pa.getSession().getId() ;
        this.problemId = pa.getProblem().getId() ;
        this.startTime = pa.getStartTime() ;
        this.endTime = pa.getEndTime() ;
        this.effectiveDuration = pa.getEffectiveDuration() ;
        this.prevState = pa.getPrevState() ;
        this.targetState = pa.getTargetState() ;
    }
}
