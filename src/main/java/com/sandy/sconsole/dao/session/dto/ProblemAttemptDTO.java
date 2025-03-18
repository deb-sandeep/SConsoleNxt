package com.sandy.sconsole.dao.session.dto;

import com.sandy.sconsole.dao.session.ProblemAttempt;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ProblemAttemptDTO implements Serializable {
    private Integer id;
    private Integer sessionId;
    private Integer topicId;
    private Integer problemId;
    private Date    startTime;
    private Date    endTime;
    private Integer effectiveDuration;
    private String  prevState;
    private String  targetState;
    
    // Do not delete the public no arg constructor. Is used by
    // spring to populate request body
    public ProblemAttemptDTO() {}
    
    public ProblemAttemptDTO( ProblemAttempt pa ) {
        this.id = pa.getId() ;
        this.sessionId = pa.getSession().getId() ;
        this.topicId = pa.getTopic().getId() ;
        this.problemId = pa.getProblem().getId() ;
        this.startTime = pa.getStartTime() ;
        this.endTime = pa.getEndTime() ;
        this.effectiveDuration = pa.getEffectiveDuration() ;
        this.prevState = pa.getPrevState() ;
        this.targetState = pa.getTargetState() ;
    }
}
