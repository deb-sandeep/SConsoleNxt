package com.sandy.sconsole.api.master.helper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookMeta {
    
    @Data
    public static class ValidationMsgCount {
        private int numError = 0 ;
        private int numWarning = 0 ;
        private int numInfo = 0 ;
    }
    
    @Data
    public static class ValidationMsg {
        public enum Type { ERROR, WARNING, INFO }
        private Type type = null ;
        private String field = null ;
        private String value = null ;
        private String msg = null ;
    }
    
    @Data
    public static class ProblemCluster {
        private String type = null ;
        private String lctSequence = null ;
        private int startIndex ;
        private int endIndex ;
    }
    
    @Data
    public static class ExerciseMeta {
        private String name = null ;
        private List<String> problems = new ArrayList<>() ;
        
        @JsonIgnore
        private List<ProblemCluster> problemClusters = new ArrayList<>() ;
        
        private List<ValidationMsg> validationMsgs = new ArrayList<>() ;
        private ValidationMsgCount msgCount = new ValidationMsgCount() ;
    }
    
    @Data
    public static class ChapterMeta {
        private String title = null ;
        private List<ExerciseMeta> exercises = new ArrayList<>() ;
        
        private List<ValidationMsg> validationMsgs = new ArrayList<>() ;
        private ValidationMsgCount  msgCount = new ValidationMsgCount() ;
    }
    
    private String subject = null ;
    private String series = null ;
    private String name = null ;
    private String author = null ;
    private String shortName = null ;
    private List<ChapterMeta> chapters = new ArrayList<>() ;
    
    private List<ValidationMsg> validationMsgs = new ArrayList<>() ;
    private ValidationMsgCount  msgCount       = new ValidationMsgCount() ;
    private ValidationMsgCount  totalMsgCount  = new ValidationMsgCount() ;
    
    public void updateMsgCount() {
        updateMsgCount( validationMsgs, msgCount ) ;
        for( ChapterMeta chapter : chapters ) {
            updateMsgCount( chapter.validationMsgs, chapter.msgCount ) ;
            for( ExerciseMeta exercise : chapter.exercises ) {
                updateMsgCount( exercise.validationMsgs, exercise.msgCount ) ;
            }
        }
    }
    
    private void aggregateTotalMsgCount( ValidationMsgCount counter ) {
        this.totalMsgCount.numError += counter.numError ;
        this.totalMsgCount.numWarning += counter.numWarning ;
        this.totalMsgCount.numInfo += counter.numInfo ;
    }
    
    private void updateMsgCount( List<ValidationMsg> msgs, ValidationMsgCount counter ) {
        for( ValidationMsg msg : msgs ) {
            switch( msg.getType() ) {
                case ERROR -> counter.numError++;
                case WARNING -> counter.numWarning++;
                case INFO -> counter.numInfo++;
            }
        }
        aggregateTotalMsgCount( counter ) ;
    }
    
    public static ValidationMsg errMsg( String field, String msg ) {
        return createValidationMsg( BookMeta.ValidationMsg.Type.ERROR, field, null, msg ) ;
    }
    
    public static  BookMeta.ValidationMsg errMsg( String field, String value, String msg ) {
        return createValidationMsg( BookMeta.ValidationMsg.Type.ERROR, field, value, msg ) ;
    }
    
    public static  BookMeta.ValidationMsg warnMsg( String field, String msg ) {
        return createValidationMsg( BookMeta.ValidationMsg.Type.WARNING, field, null, msg ) ;
    }
    
    public static  BookMeta.ValidationMsg warnMsg( String field, String value, String msg ) {
        return createValidationMsg( BookMeta.ValidationMsg.Type.WARNING, field, value, msg ) ;
    }
    
    public static  BookMeta.ValidationMsg infoMsg( String field, String msg ) {
        return createValidationMsg( BookMeta.ValidationMsg.Type.INFO, field, null, msg ) ;
    }
    
    public static  BookMeta.ValidationMsg infoMsg( String field, String value, String msg ) {
        return createValidationMsg( BookMeta.ValidationMsg.Type.INFO, field, value, msg ) ;
    }
    
    public static  BookMeta.ValidationMsg createValidationMsg(
                                            BookMeta.ValidationMsg.Type type,
                                            String field,
                                            String value,
                                            String msg ) {
        
        BookMeta.ValidationMsg vMsg = new BookMeta.ValidationMsg() ;
        vMsg.setType( type ) ;
        vMsg.setField( field ) ;
        vMsg.setValue( value );
        vMsg.setMsg( msg ) ;
        return vMsg ;
    }
    
}
