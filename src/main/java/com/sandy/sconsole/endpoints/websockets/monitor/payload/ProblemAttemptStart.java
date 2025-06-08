package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import lombok.Data;

import java.util.Date;

@Data
public class ProblemAttemptStart {

    private int sessionId ;
    private int problemAttemptId ;
    private Date startTime ;
    private String syllabusName ;
    private String topicName ;
    private String bookName ;
    private int chapterNum ;
    private String chapterName ;
    private String problemKey ;
    private String currentState ;
}
