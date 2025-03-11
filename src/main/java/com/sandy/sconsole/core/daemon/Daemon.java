package com.sandy.sconsole.core.daemon;

import lombok.Getter;

@Getter
public abstract class Daemon extends Thread {

    private final String daemonName ;

    protected Daemon( String name ) {
        this.daemonName = name ;
        super.setDaemon( true ) ;
    }
}
