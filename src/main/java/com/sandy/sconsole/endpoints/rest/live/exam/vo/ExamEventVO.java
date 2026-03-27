package com.sandy.sconsole.endpoints.rest.live.exam.vo;

import com.sandy.sconsole.dao.exam.ExamEventLog;
import lombok.Data;

import java.time.Instant;

@Data
public class ExamEventVO {

    private int id ;
    private int examAttemptId ;
    private int sequence ;
    private String eventType ;
    private String eventName ;
    private String  payload ;
    private Instant creationTime ;
    private long timeMarker ;
    
    public ExamEventVO() {}
    
    public ExamEventVO( ExamEventLog e ) {
        this.id = e.getId() ;
        this.examAttemptId = e.getExamAttempt().getId() ;
        this.sequence = e.getSequence() ;
        this.eventType = e.getEventType() ;
        this.eventName = e.getEventName() ;
        this.payload = e.getPayload() ;
        this.creationTime = e.getCreationTime() ;
        this.timeMarker = e.getTimeMarker() ;
    }
}
