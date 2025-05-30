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
        
        static ExecutionResult ERROR( String message ) {
            return new ExecutionResult( Status.ERROR, message ) ;
        }
        
        static ExecutionResult ERROR( Throwable t ) {
            return new ExecutionResult( Status.ERROR, t.getMessage(), t ) ;
        }
        
        static ExecutionResult ERROR( String message, Throwable t ) {
            return new ExecutionResult( Status.ERROR, message, t ) ;
        }
        
        public enum Status { OK, ERROR }
        
        private Status status ;
        private String message ;
        private String exceptionTrace ;
        
        public ExecutionResult( Status status, String message ) {
            this( status, message, null ) ;
        }
        
        public ExecutionResult( Status status, String message, Throwable e ) {
            this.status = status ;
            this.message = message ;
            this.exceptionTrace = ExceptionUtils.getStackTrace( e ) ;
        }
    }
    
    private T data ;
    private ExecutionResult executionResult ;
    
    public AR( T data ) {
        this( ExecutionResult.OK, data ) ;
    }
    
    public AR( ExecutionResult result ) {
        this( result, null ) ;
    }

    public AR( ExecutionResult result, T data ) {
        this.data = data ;
        this.executionResult = result ;
    }
    
    public static ResponseEntity<AR<String>> success() {
        return success( "Success" ) ;
    }

    public static <T> ResponseEntity<AR<T>> success( T data ) {
        return ResponseEntity.ok( new AR<>( data ) ) ;
    }
    
    public static <T> ResponseEntity<AR<T>> functionalError( String message ) {
        log.error( "Functional error : {}", message ) ;
        return ResponseEntity.ok( new AR<>( ExecutionResult.ERROR( message ) ) ) ;
    }
    
    public static <T> ResponseEntity<AR<T>> functionalError( String message, Throwable cause ) {
        log.error( "Functional error : {}", message ) ;
        return ResponseEntity.ok( new AR<>( ExecutionResult.ERROR( message, cause ) ) ) ;
    }
    
    public static <T> ResponseEntity<AR<T>> systemError( Throwable t ) {
        log.error( "Internal Server Error", t ) ;
        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                .body( new AR<>( ExecutionResult.ERROR( t ) ) ) ;
    }
    
    public static <T> ResponseEntity<AR<T>> badRequest( String message ) {
        return ResponseEntity.status( HttpStatus.BAD_REQUEST )
                .body( new AR<>( ExecutionResult.ERROR( message ) ) ) ;
    }
}
