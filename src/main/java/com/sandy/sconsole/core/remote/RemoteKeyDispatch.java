package com.sandy.sconsole.core.remote;

import com.sandy.sconsole.SConsole;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class RemoteKeyDispatch implements Runnable {

    private final LinkedBlockingQueue<RemoteKeyEvent> keyEvents = new LinkedBlockingQueue<>() ;

    private Thread eventDispatchThread ;
    private boolean keepRunning = true ;

    @Autowired private SConsole sconsole ;

    @PostConstruct
    public void initialize() throws Exception {
        log.debug( "Initializing RemoteKey Dispatch" ) ;
        eventDispatchThread = new Thread( this ) ;
    }

    @PreDestroy
    public void shutdown() {
        log.debug( "Shutting down RemoteKey Dispatch" ) ;
        this.keepRunning = false ;
        this.eventDispatchThread.interrupt() ;
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
