package com.sandy.sconsole.api.master.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TopicProblemCountVO {
    
    private int topicId ;
    private int numProblems ;
    private Map<String, Integer> problemTypeCount = new HashMap<>();
    
    public TopicProblemCountVO( int topicId ) {
        this.topicId = topicId ;
    }
    
    public void addCount( String problemType, int numProblems ) {
        problemTypeCount.put( problemType, numProblems ) ;
        this.numProblems += numProblems ;
    }
}
