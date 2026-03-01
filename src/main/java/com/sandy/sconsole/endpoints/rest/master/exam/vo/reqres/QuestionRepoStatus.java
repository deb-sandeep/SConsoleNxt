package com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandy.sconsole.dao.exam.repo.QuestionRepo;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class QuestionRepoStatus {
    
    @Data
    public static class SyllabusStatus {
        private String syllabusName ;
        private List<TopicStatus> topicStats = new ArrayList<>() ;
        private int numQuestions ;
        private String color ;
        private String iconName ;
        
        @JsonIgnore
        private Map<Integer, TopicStatus> topicStatusMap = new HashMap<>() ;
        
        public SyllabusStatus( String syllabusName ) {
            this.syllabusName = syllabusName ;
        }
        
        public void build( QuestionRepo.RepoStatusRow s ) {
            TopicStatus t = topicStatusMap.get( s.getTopicId() ) ;
            if( t == null ) {
                t = new TopicStatus( s.getTopicId(), s.getTopicName() ) ;
                topicStatusMap.put( s.getTopicId(), t ) ;
                topicStats.add( t ) ;
            }
            t.build( s ) ;
            numQuestions += s.getCount() ;
        }
    }
    
    @Data
    public static class TopicStatus {
        private int topicId ;
        private String topicName ;
        private int numQuestions ;
        
        private Map<String, QTypeStatus> questionTypeStats = new HashMap<>() ;
        
        public TopicStatus( int topicId, String name ) {
            this.topicId = topicId ;
            this.topicName = name ;
        }
        
        public void build( QuestionRepo.RepoStatusRow s ) {
            QTypeStatus status = questionTypeStats.computeIfAbsent( s.getProblemType(), QTypeStatus::new ) ;
            status.build( s ) ;
            numQuestions += s.getCount() ;
        }
    }
    
    @Data
    public static class QTypeStatus {
        private String type ;
        private int numUnassigned = 0 ;
        private int numAssigned = 0 ;
        private int numAttempted = 0 ;
        private int numQuestions = 0 ;
    
        public QTypeStatus( String type ) {
            this.type = type ;
        }
        
        public void build( QuestionRepo.RepoStatusRow s ) {
            switch( s.getState() ) {
                case "UNASSIGNED" -> numUnassigned += s.getCount() ;
                case "ASSIGNED" -> numAssigned += s.getCount() ;
                case "ATTEMPTED" -> numAttempted += s.getCount() ;
            }
            numQuestions += s.getCount() ;
        }
    }
    
    private Map<String, SyllabusStatus> syllabusStatusMap = new HashMap<>() ;
    private int numQuestions ;
    
    @JsonIgnore
    private final SyllabusRepo syllabusRepo ;
    
    public QuestionRepoStatus( SyllabusRepo syllabusRepo ) {
        this.syllabusRepo = syllabusRepo ;
    }
    
    public void build( QuestionRepo.RepoStatusRow s ) {
        SyllabusStatus syllabusStatus = syllabusStatusMap.computeIfAbsent( s.getSyllabusName(), name -> {
            Syllabus syllabus = syllabusRepo.findById( name ).get() ;
            SyllabusStatus status = new SyllabusStatus( name ) ;
            status.setColor( syllabus.getColor() ) ;
            status.setIconName( syllabus.getIconName() ) ;
            return status ;
        } ) ;
        syllabusStatus.build( s ) ;
        numQuestions += s.getCount() ;
    }
}
