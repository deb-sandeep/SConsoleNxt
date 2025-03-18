package com.sandy.sconsole.dao.session.dto;

import com.sandy.sconsole.dao.session.Session;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class SessionDTO implements Serializable {
    private int     id;
    private Date    startTime;
    private Date    endTime;
    private String  sessionType;
    private int     topicId;
    private String  topicName;
    private String  syllabusName;
    private int     effectiveDuration;
    
    // Do not delete the public no arg constructor. Is used by
    // spring to populate request body
    public SessionDTO() {}
    
    public SessionDTO( Session session ) {
        this.id = session.getId() ;
        this.startTime = session.getStartTime() ;
        this.endTime = session.getEndTime() ;
        this.sessionType = session.getSessionType() ;
        this.topicId = session.getTopic().getId() ;
        this.topicName = session.getTopic().getTopicName() ;
        this.syllabusName = session.getSyllabusName() ;
        this.effectiveDuration = session.getEffectiveDuration() ;
    }
    
    public SessionDTO( SessionDTO session ) {
        this.id = session.id ;
        this.absorb( session ) ;
    }
    
    public int getDuration() {
        return (int)((endTime.getTime() - startTime.getTime())/1000) ;
    }
    
    public void absorb( @NonNull SessionDTO session ) {
        this.startTime = session.startTime ;
        this.endTime = session.endTime ;
        this.sessionType = session.sessionType ;
        this.topicId = session.topicId ;
        this.topicName = session.topicName ;
        this.syllabusName = session.syllabusName ;
        this.effectiveDuration = session.effectiveDuration ;
    }
}
