package com.sandy.sconsole.api.master.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class BookMetaVO {
    
    @Data
    public static class ValidationMsgCount {
        private int numError = 0 ;
        private int numWarning = 0 ;
        private int numInfo = 0 ;
        
        @JsonProperty( "total" )
        public int getTotalMessages() {
            return numError + numWarning + numInfo ;
        }
        
        public void aggregateCount( ValidationMsgCount anotherCounter ) {
            this.numError   += anotherCounter.numError ;
            this.numWarning += anotherCounter.numWarning ;
            this.numInfo    += anotherCounter.numInfo ;
        }
    }
    
    @Data
    public static class ValidationMsg {
        
        public enum Type { ERROR, WARNING, INFO }
        private Type type ;
        private String msg ;

        public ValidationMsg( Type type, String msg ) {
            this.type = type ;
            this.msg = msg ;
        }
    }
    
    @Data
    public static class ValidationMessages {
        
        private Map<String, List<ValidationMsg>> messages = new HashMap<>() ;

        @JsonProperty( "msgCount" )
        public ValidationMsgCount getMsgCount() {
            ValidationMsgCount msgCount = new ValidationMsgCount() ;
            for( List<ValidationMsg> msgs : messages.values() ) {
                for( ValidationMsg msg : msgs ) {
                    switch( msg.getType() ) {
                        case ERROR -> msgCount.numError++;
                        case WARNING -> msgCount.numWarning++;
                        case INFO -> msgCount.numInfo++;
                    }
                }
            }
            return msgCount ;
        }
        
        public void addError( String field, String msg ) {
            addMsg( field, ValidationMsg.Type.ERROR, msg ) ;
        }
        
        public void addWarning( String field, String msg ) {
            addMsg( field, ValidationMsg.Type.WARNING, msg ) ;
        }
        
        public void addInfo( String field, String msg ) {
            addMsg( field, ValidationMsg.Type.INFO, msg ) ;
        }

        private void addMsg( String field, ValidationMsg.Type type, String msg ) {
            List<ValidationMsg> msgs = messages.computeIfAbsent( field, k -> new ArrayList<>() );
            msgs.add( new ValidationMsg( type, msg ) ) ;
        }
    }
    
    @Data
    public static class ProblemCluster {
        private String metadata ;
        private String type = null ;
        private String extraQualifier = null ;
        private String lctSequence = null ;
        private int startIndex ;
        private int endIndex ;
        
        public ProblemCluster( String metadata ) {
            this.metadata = metadata ;
        }
    }
    
    @Data
    public static class ExerciseMeta {
        private String name = null ;
        private List<String> problems = new ArrayList<>() ;
        
        @JsonIgnore
        private List<ProblemCluster> problemClusters = new ArrayList<>() ;

        private ValidationMessages validationMessages = new ValidationMessages() ;
    }
    
    @Data
    public static class ChapterMeta {
        private String title = null ;
        private List<ExerciseMeta> exercises = new ArrayList<>() ;
        
        // Chapter number and name are derived during parsing from the
        // title which is of the format <num> - <name>
        @JsonIgnore
        private int chapterNum = 0 ;
        
        @JsonIgnore
        private String chapterName = null ;
        
        private ValidationMessages validationMessages = new ValidationMessages() ;
        private ValidationMsgCount totalMsgCount = new ValidationMsgCount() ;
        
        @JsonProperty( "totalMsgCount" )
        public ValidationMsgCount getTotalMsgCount() {
            ValidationMsgCount totalMsgCount = new ValidationMsgCount() ;
            
            totalMsgCount.aggregateCount( validationMessages.getMsgCount() ) ;
            for( ExerciseMeta exercise : this.exercises ) {
                totalMsgCount.aggregateCount( exercise.validationMessages.getMsgCount() ) ;
            }
            return totalMsgCount ;
        }
    }
    
    private String subject = null ;
    private String series = null ;
    private String name = null ;
    private String author = null ;
    private String shortName = null ;
    private List<ChapterMeta> chapters = new ArrayList<>() ;
    
    private ValidationMessages validationMessages = new ValidationMessages() ;
    private ValidationMsgCount totalMsgCount  = new ValidationMsgCount() ;
    
    private String serverFileName = null ;
    
    @JsonProperty( "totalMsgCount" )
    public ValidationMsgCount getTotalMsgCount() {
        ValidationMsgCount totalMsgCount = new ValidationMsgCount() ;
        totalMsgCount.aggregateCount( validationMessages.getMsgCount() ) ;
        for( ChapterMeta chapter : this.chapters ) {
            totalMsgCount.aggregateCount( chapter.getTotalMsgCount() ) ;
        }
        return totalMsgCount ;
    }
}
