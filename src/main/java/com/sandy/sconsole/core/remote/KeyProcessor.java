package com.sandy.sconsole.core.remote;

public interface KeyProcessor {
    public KeySet getConsumableKeySet() ;
    public void processKeyEvent( RemoteKeyEvent keyEvent ) ;
}
