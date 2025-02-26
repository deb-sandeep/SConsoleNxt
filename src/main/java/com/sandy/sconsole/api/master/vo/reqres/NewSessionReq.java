package com.sandy.sconsole.api.master.vo.reqres;

import lombok.Data;

import java.util.Date;

@Data
public class NewSessionReq {
    private String sessionType ;
    private int    topicId ;
    private String syllabusName ;
    private Date   startTime ;
}
