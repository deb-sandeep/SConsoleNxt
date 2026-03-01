package com.sandy.sconsole.endpoints.rest.master.book.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TopicProblemCountVO {
    
    private int topicId ;
    
    private int numProblems ;
    private Map<String, Integer> problemTypeCount = new HashMap<>() ;
    
    private int numRemainingProblems ;
    private Map<String, Integer> remainingProblemTypeCount = new HashMap<>() ;
    
    public TopicProblemCountVO( int topicId ) {
        this.topicId = topicId ;
    }
    
    public void addCount( String problemType, int numProblems ) {
        problemTypeCount.put( problemType, numProblems ) ;
        remainingProblemTypeCount.put( problemType, 0 ) ;
        this.numProblems += numProblems ;
    }
    
    public void addRemainingCount( String problemType, int numProblems ) {
        remainingProblemTypeCount.put( problemType, numProblems ) ;
        this.numRemainingProblems += numProblems ;
    }
}
