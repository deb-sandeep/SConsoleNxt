package com.sandy.sconsole.core.remote;

public interface KeyProcessor {
    
    KeySet getConsumableKeySet() ;
    
    void processKeyEvent( RemoteKeyEvent keyEvent ) ;
}
