package com.sandy.sconsole.dao.session.dto;

import com.sandy.sconsole.dao.session.SessionPause;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SessionPauseDTO implements Serializable {
    
    private int  id;
    private int  sessionId;
    private Date startTime;
    private Date endTime;
    
    public SessionPauseDTO() {}
    
    public SessionPauseDTO( SessionPause dao ) {
        this.id = dao.getId() ;
        this.sessionId = dao.getSession().getId() ;
        this.startTime = dao.getStartTime() ;
        this.endTime = dao.getEndTime() ;
    }
    
    public SessionPauseDTO( SessionPauseDTO pauseDTO ) {
        this.id = pauseDTO.id ;
        this.sessionId = pauseDTO.sessionId ;
        this.startTime = pauseDTO.startTime ;
        this.endTime = pauseDTO.endTime ;
    }
    
    public int getDuration() {
        return (int)((endTime.getTime() - startTime.getTime())/1000) ;
    }
}
