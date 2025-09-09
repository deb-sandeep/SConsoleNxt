package com.sandy.sconsole.core.api.client.chemspider;

public class ChemSpiderException extends Exception {
    public ChemSpiderException( String message ) {
        super( message ) ;
    }
    
    public ChemSpiderException( String message, Throwable cause ) {
        super( message, cause ) ;
    }
}
