package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import lombok.Data;

import java.util.Date;

@Data
public class ProblemAttemptEnd {

    private int sessionId ;
    private int problemAttemptId ;
    private Date startTime ;
    private Date endTime ;
    private String syllabusName ;
    private String topicName ;
    private String bookName ;
    private int chapterNum ;
    private String chapterName ;
    private String problemKey ;
    private String targetState ;
    private int effectiveDuration ;
}
