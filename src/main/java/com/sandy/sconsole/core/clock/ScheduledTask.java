package com.sandy.sconsole.core.clock;

import org.slf4j.MDC;

import static com.sandy.sconsole.core.log.LogIndenter.THREAD_NAME_KEY;

public abstract class ScheduledTask implements Runnable {

    @Override
    public final void run() {
        try {
            MDC.put( THREAD_NAME_KEY, "clockDaemon" ) ;
            executeTask() ;
        }
        finally {
            MDC.remove( THREAD_NAME_KEY ) ;
        }
    }

    protected abstract void executeTask() ;
}
