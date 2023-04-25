package com.sandy.sconsole.core.bus;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * An implementation of {@link EventSubscriber}, which wraps around concrete
 * implementations of subscribers and dispatches events to them in an
 * asynchronous fashion.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
@Slf4j
class AsyncEventDispatchProxy implements EventSubscriber, Runnable {

    private final EventSubscriber subscriber ;
    private final Thread dispatchThread ;
    private boolean stop = false ;

    /** The unbounded queue in which events are stored before dispatching. */
    private final LinkedBlockingQueue<Event> eventQueue =
            new LinkedBlockingQueue<>() ;

    public AsyncEventDispatchProxy( final EventSubscriber subscriber ) {
        
        this.subscriber = subscriber ;
        this.dispatchThread = new Thread( this ) ;
        this.dispatchThread.setDaemon( true ) ;
        this.dispatchThread.start() ;
    }

    public void run() {
        while( !this.stop ) {
            Event evt = null ;
            try {
                evt = this.eventQueue.take() ;
                this.subscriber.handleEvent( evt ) ;
            }
            catch( InterruptedException ie ) {
                // IE can be generated either for a graceful shutdown or by
                // a burst of cosmic rays. In the former case, the stop 
                // flag would be set to true and this loop will gracefully 
                // terminate. In case of cosmic rays, we gobble up the exception
                // without a burp and let the loop continue.
            }
            catch ( Throwable e ) {
                log.error( "Dispatch failed for event " + evt, e ) ;
            }
        }
        this.eventQueue.clear() ;
    }

    public void handleEvent( final Event event ) {
        this.eventQueue.add( event ) ;
    }

    public EventSubscriber getSubscriber() {
        return this.subscriber ;
    }

    public void stop() {
        this.stop = true ;
        this.dispatchThread.interrupt() ;
    }

    public boolean equals( final Object obj ) {
        if( obj instanceof AsyncEventDispatchProxy ) {
            return this.subscriber.equals( obj ) ;
        }
        return false ;
    }

    public int hashCode() {
        return this.subscriber.hashCode() ;
    }
}
