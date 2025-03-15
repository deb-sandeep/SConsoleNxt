package com.sandy.sconsole.dao.session.dto;

import com.sandy.sconsole.dao.session.Session;
import lombok.Data;
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
    
    public SessionDTO( SessionDTO sessionDTO ) {
        this.id = sessionDTO.id ;
        this.startTime = sessionDTO.startTime ;
        this.endTime = sessionDTO.endTime ;
        this.sessionType = sessionDTO.sessionType ;
        this.topicId = sessionDTO.topicId ;
        this.topicName = sessionDTO.topicName ;
        this.syllabusName = sessionDTO.syllabusName ;
        this.effectiveDuration = sessionDTO.effectiveDuration ;
    }
    
    public int getDuration() {
        return (int)((endTime.getTime() - startTime.getTime())/1000) ;
    }
}
