package com.sandy.sconsole.core.bus;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

@Slf4j
class AsyncEventDispatchProxy implements EventSubscriber, Runnable {
    
    @Getter private final EventSubscriber subscriber ;
    
    private final ExecutorService executor ;
    
    private Event event = null ;

    public AsyncEventDispatchProxy( final EventSubscriber subscriber,
                                    final ExecutorService executor ) {
        
        this.subscriber = subscriber ;
        this.executor = executor ;
    }
    
    public void handleEvent( final Event event ) {
        this.event = event ;
        this.executor.execute( this ) ;
    }
    
    public void run() {
        this.subscriber.handleEvent( event ) ;
    }
}
