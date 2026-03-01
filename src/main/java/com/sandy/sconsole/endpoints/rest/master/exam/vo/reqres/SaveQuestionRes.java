package com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres;

import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
public class SaveQuestionRes {

    private boolean questionSaved ;
    private String  errorMsg ;
    private int     questionId ;
    
    public static SaveQuestionRes success( int questionId ) {
        SaveQuestionRes res = new SaveQuestionRes() ;
        res.setQuestionSaved( true ) ;
        res.setQuestionId( questionId ) ;
        return res ;
    }
    
    public static SaveQuestionRes error( String errMsg, Throwable t ) {
        StringBuilder sb = new StringBuilder( errMsg ) ;
        if( t != null ) {
            sb.append( " \n " ).append( ExceptionUtils.getStackTrace( t ) ) ;
        }
        
        SaveQuestionRes res = new SaveQuestionRes() ;
        res.setQuestionSaved( false ) ;
        res.setErrorMsg( sb.toString() ) ;
        
        return res ;
    }
}
