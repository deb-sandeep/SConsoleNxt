package com.sandy.sconsole.core.remote;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentFinalizer;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class RemoteKeyDispatch
        implements Runnable, ComponentInitializer, ComponentFinalizer {

    private final LinkedBlockingQueue<RemoteKeyEvent> keyEvents = new LinkedBlockingQueue<>() ;

    private Thread eventDispatchThread ;
    private boolean keepRunning = true ;

    private SConsole sconsole ;

    @Override
    public boolean isInvocable() { return true ; }

    public void initialize( SConsole app ) throws Exception {
        eventDispatchThread = new Thread( this ) ;
    }

    public void destroy( SConsole app ) throws Exception {
        log.debug( "Finalizing RemoteKey Dispatch." ) ;
        if( eventDispatchThread != null ) {
            this.keepRunning = false ;
            eventDispatchThread.interrupt() ;
        }
    }

    public void addKeyEvent( RemoteKeyEvent keyEvent ) {
        keyEvents.add( keyEvent ) ;
    }

    @Override
    public void run() {
        RemoteKeyEvent event ;
        try {
            while( keepRunning ) {
                event = keyEvents.take() ;
                log.debug( "Dispatching remote key {}", event ) ;
                if( !keepRunning ) {
                    break ;
                }
                else {
                    try {
                        sconsole.getFrame().handleRemoteKeyEvent( event ) ;
                    }
                    catch( Exception e ) {
                        log.error( "RemoteKey dispatch generated an exception.", e ) ;
                        // Do not propagate. The event dispatch thread doesn't
                        // care if few dispatches were not received properly.
                    }
                }
            }
        }
        catch( InterruptedException e ) {
            log.error( "Remote key event dispatch thread interrupted." ) ;
        }
    }
}
