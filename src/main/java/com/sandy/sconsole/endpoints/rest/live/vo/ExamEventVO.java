package com.sandy.sconsole.endpoints.rest.live.vo;

import com.sandy.sconsole.dao.exam.ExamEventLog;
import lombok.Data;

import java.time.Instant;

@Data
public class ExamEventVO {

    private int id ;
    private int examAttemptId ;
    private int sequence ;
    private String eventId ;
    private String  payload ;
    private Instant creationTime ;
    private long    timeMarker ;
    
    public ExamEventVO() {}
    
    public ExamEventVO( ExamEventLog e ) {
        this.id = e.getId() ;
        this.examAttemptId = e.getExamAttempt().getId() ;
        this.sequence = e.getSequence() ;
        this.eventId = e.getEventId() ;
        this.payload = e.getPayload() ;
        this.creationTime = e.getCreationTime() ;
        this.timeMarker = e.getTimeMarker() ;
    }
}
