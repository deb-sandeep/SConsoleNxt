package com.sandy.sconsole.core.daemon;

import lombok.Getter;

@Getter
public abstract class DaemonBase extends Thread {

    private final String daemonName ;

    protected DaemonBase( String name ) {
        this.daemonName = name ;
        super.setDaemon( true ) ;
    }
}
