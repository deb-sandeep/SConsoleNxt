package com.sandy.sconsole.core.bus;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class SyncEventDispatchProxy implements EventSubscriber {
    
    @Getter private final EventSubscriber subscriber ;
    @Getter private final int priority ;

    public SyncEventDispatchProxy( final EventSubscriber subscriber, int priority ) {
        this.subscriber = subscriber ;
        this.priority = priority ;
    }
    
    public void handleEvent( final Event event ) {
        this.subscriber.handleEvent( event ) ;
    }
}
