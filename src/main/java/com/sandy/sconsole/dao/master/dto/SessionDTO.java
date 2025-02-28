package com.sandy.sconsole.dao.master.dto;

import com.sandy.sconsole.dao.master.Session;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class SessionDTO implements Serializable {
    private int     id;
    private Instant startTime;
    private Instant endTime;
    private String  sessionType;
    private int     topicId;
    private String  syllabusName;
    private int     effectiveDuration;
    
    public SessionDTO() {}
    
    public SessionDTO( Session session ) {
        this.id = session.getId() ;
        this.startTime = session.getStartTime() ;
        this.endTime = session.getEndTime() ;
        this.sessionType = session.getSessionType() ;
        this.topicId = session.getTopic().getId() ;
        this.syllabusName = session.getSyllabusName() ;
        this.effectiveDuration = session.getEffectiveDuration() ;
    }
}
