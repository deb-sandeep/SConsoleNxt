package com.sandy.sconsole.dao.session.dto;

import com.sandy.sconsole.dao.session.SessionPause;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class SessionPauseDTO implements Serializable {
    
    private int     id;
    private int     sessionId;
    private Instant startTime;
    private Instant endTime;
    
    public SessionPauseDTO() {}
    
    public SessionPauseDTO( SessionPause dao ) {
        this.id = dao.getId() ;
        this.sessionId = dao.getSession().getId() ;
        this.startTime = dao.getStartTime() ;
        this.endTime = dao.getEndTime() ;
    }
}
