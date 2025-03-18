package com.sandy.sconsole.dao.session.dto;

import com.sandy.sconsole.dao.session.SessionPause;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
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
    
    public SessionPauseDTO( SessionPauseDTO pause ) {
        this.id = pause.id ;
        this.absorb( pause );
    }
    
    public int getDuration() {
        return (int)((endTime.getTime() - startTime.getTime())/1000) ;
    }
    
    public void absorb( @NonNull SessionPauseDTO pause ) {
        this.sessionId = pause.sessionId ;
        this.startTime = pause.startTime ;
        this.endTime = pause.endTime ;
    }
}
