package com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres;

import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
public class SaveExamRes {

    private String  errorMsg ;
    private int     examId ;
    
    public static SaveExamRes success( int examId ) {
        SaveExamRes res = new SaveExamRes() ;
        res.setExamId( examId ) ;
        return res ;
    }
    
    public static SaveExamRes error( String errMsg, Throwable t ) {
        StringBuilder sb = new StringBuilder( errMsg ) ;
        if( t != null ) {
            sb.append( " \n " ).append( ExceptionUtils.getStackTrace( t ) ) ;
        }
        
        SaveExamRes res = new SaveExamRes() ;
        res.setErrorMsg( sb.toString() ) ;
        return res ;
    }
}
