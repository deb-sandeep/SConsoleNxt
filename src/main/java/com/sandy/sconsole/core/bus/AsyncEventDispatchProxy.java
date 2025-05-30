package com.sandy.sconsole.core.bus;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
class AsyncEventDispatchProxy implements EventSubscriber, Runnable {
    
    @Getter private final EventSubscriber subscriber ;
    
    @Getter private Event event = null ;
    
    private final Cache<Event, Long> recentEvents = CacheBuilder.newBuilder()
            .expireAfterWrite( 500, TimeUnit.MILLISECONDS ) // 500ms window
            .build() ;
    
    private final ExecutorService executor ;
    private final EventBus eventBus ;

    public AsyncEventDispatchProxy( final EventSubscriber subscriber,
                                    final ExecutorService executor,
                                    final EventBus eventBus ) {
        
        this.subscriber = subscriber ;
        this.executor = executor ;
        this.eventBus = eventBus ;
    }
    
    public void handleEvent( final Event event ) {
        
        if( recentEvents.getIfPresent( event ) == null ) {
            this.event = event ;
            this.executor.execute( this ) ;
            recentEvents.put( event, System.currentTimeMillis() ) ;
        }
        else {
            log.debug( "Ignoring event : {} to subscriber {}@{} as it was received recently",
                       eventBus.getEventName( event.getEventId() ),
                       subscriber.getClass().getSimpleName(),
                       subscriber.hashCode() ) ;
        }
    }
    
    public void run() {
        this.subscriber.handleEvent( event ) ;
    }
}
