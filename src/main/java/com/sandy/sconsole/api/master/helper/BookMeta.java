package com.sandy.sconsole.api.master.helper;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookMeta {
    
    @Data
    public static class ValidationMsg {
        public enum Type { ERROR, WARNING, INFO }
        
        private Type type = null ;
        private String field = null ;
        private String value = null ;
        private String msg = null ;
    }
    
    @Data
    public static class ExerciseMeta {
        
        private String              name           = null ;
        private List<String>        problems       = new ArrayList<>() ;
        private List<ValidationMsg> validationMsgs = new ArrayList<>() ;
    }
    
    @Data
    public static class ChapterMeta {
        
        private String title = null ;
        private List<ExerciseMeta> exercises = new ArrayList<>() ;
        private List<ValidationMsg> validationMsgs = new ArrayList<>() ;
    }
    
    private String subject = null ;
    private String series = null ;
    private String name = null ;
    private String author = null ;
    private String shortName = null ;
    private List<ChapterMeta> chapters  = new ArrayList<>() ;
    
    private List<ValidationMsg> validationMsgs = new ArrayList<>() ;
    
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
