package com.sandy.sconsole.core.api ;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * All API responses are wrapped in this class. This class provides for wrapping
 * the functional response and any error messages that may have been generated.
 */
@Slf4j
@Data
public class AR<T> {
    
    @Data
    public static class ExecutionResult {
        
        public static ExecutionResult OK = new ExecutionResult( Status.OK, "Processing success" ) ;
        public static ExecutionResult ERROR = new ExecutionResult( Status.ERROR, "Processing failure" ) ;
        
        public enum Status { OK, ERROR }
        
        private Status status ;
        private String message ;
        private String exceptionTrace ;
        
        public ExecutionResult( Status status, String message ) {
            this( status, message, null ) ;
        }
        
        public ExecutionResult( Throwable e ) {
            this( Status.ERROR, e.getMessage(), e ) ;
        }
        
        public ExecutionResult( Status status, String message, Throwable e ) {
            this.status = status ;
            this.message = message ;
            this.exceptionTrace = ExceptionUtils.getStackTrace( e ) ;
        }
    }
    
    public static <T> ResponseEntity<AR<T>> success( T data ) {
        return ResponseEntity.ok( new AR<>( data ) ) ;
    }
    
    public static <T> ResponseEntity<AR<T>> failure( Throwable e ) {
        log.error( "Internal Server Error", e ) ;
        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                             .body( new AR<>( e ) ) ;
    }
    
    private T data ;
    private ExecutionResult executionResult ;
    
    public AR( T data ) {
        this.data = data ;
        this.executionResult = ExecutionResult.OK ;
    }
    
    public AR( Throwable t ) {
        this.data = null ;
        this.executionResult = new ExecutionResult( t ) ;
    }
}
