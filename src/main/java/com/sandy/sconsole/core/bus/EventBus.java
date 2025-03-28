package com.sandy.sconsole.core.bus;

import com.sandy.sconsole.EventCatalog;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component( "eventBus" )
public class EventBus {

    public static final int ALL_EVENTS = 0xCAFEBABE ;
    
    private final Map<Integer, List<EventSubscriber>> eventIdSubscriberMap = new HashMap<>() ;
    private final ExecutorService                     executor             = Executors.newFixedThreadPool( 10 ) ;
    
    // A class which contains public static final int definitions of events
    // This class is introspected to reverse translate the event code to
    // event name for debugging purposes
    @Setter private Class<?> eventCatalogClass ;
    
    @Setter private boolean printPublishLogs = false ;
    
    private final Map<Integer, String> eventIdVsNameMap = new HashMap<>() ;
    
    public void terminateExecutor() {
        this.executor.shutdown() ;
    }
    
    public synchronized void addSyncSubscriber( final EventSubscriber subscriber,
                                                final int ...eventIds ) {
        addSubscriber( subscriber, false, eventIds ) ;
    }
    
    public synchronized void addAsyncSubscriber( final EventSubscriber subscriber,
                                                 final int ...eventIds ) {
        addSubscriber( subscriber, true, eventIds ) ;
    }
    
    public synchronized void addSubscriber( final EventSubscriber subscriber,
                                            final boolean asyncDispatch,
                                            final int ...eventIds ) {
        
        if( eventIds == null || eventIds.length == 0 ) {
            addSubscriberToEventMap( subscriber, asyncDispatch, ALL_EVENTS ) ;
        }
        else {
            for( final int id : eventIds ) {
                addSubscriberToEventMap( subscriber, asyncDispatch, id ) ;
            }
        }
    }
    
    private void addSubscriberToEventMap( EventSubscriber subscriber,
                                          boolean asyncDispatch, int eventId ) {
        
        List<EventSubscriber> subscribers = eventIdSubscriberMap.computeIfAbsent( eventId, k -> new ArrayList<>() ) ;
        if( !isSubscriberPresent( subscribers, subscriber ) ) {
            if( asyncDispatch ) {
                subscribers.add( new AsyncEventDispatchProxy( subscriber, executor ) ) ;
            }
            else {
                subscribers.add( subscriber ) ;
            }
        }
    }
    
    private boolean isSubscriberPresent( List<EventSubscriber> subscribers,
                                         EventSubscriber subscriber ) {
        
        for( EventSubscriber s : subscribers ) {
            if( s instanceof AsyncEventDispatchProxy proxy ) {
                if( proxy.getSubscriber() == subscriber ) {
                    return true ;
                }
            }
            else if( s == subscriber ) {
                return true ;
            }
        }
        return false ;
    }
    
    public synchronized void removeSubscriber( final EventSubscriber subscriber,
                                               int ...eventIds ) {
        
        if( eventIds == null || eventIds.length == 0 ) {
            removeSubscriberFromEventMap( subscriber, ALL_EVENTS ) ;
            for( Integer eventId : eventIdSubscriberMap.keySet() ) {
                removeSubscriberFromEventMap( subscriber, eventId ) ;
            }
        }
        else {
            for( final int id : eventIds ) {
                removeSubscriberFromEventMap( subscriber, id ) ;
            }
        }
    }
    
    private void removeSubscriberFromEventMap( EventSubscriber subscriber, int eventId ) {
        
        List<EventSubscriber> subscribers = eventIdSubscriberMap.get( eventId ) ;
        if( subscribers != null ) {
            for( Iterator<EventSubscriber> esIter = subscribers.iterator(); esIter.hasNext(); ) {
                
                EventSubscriber regSubscriber = esIter.next() ;
                
                if( regSubscriber instanceof AsyncEventDispatchProxy ) {
                    regSubscriber = (( AsyncEventDispatchProxy )regSubscriber).getSubscriber() ;
                }
                
                if( regSubscriber == subscriber ) {
                    esIter.remove() ;
                }
            }
        }
    }
    
    public synchronized void publishEvent( final int eventId ) {
        publishEvent( eventId, null ) ;
    }
    
    public synchronized void publishEvent( final int eventId, final Object value ) {
        
        if( printPublishLogs ) {
            log.debug( "Publishing event {}", getEventName( eventId ) ) ;
        }
        
        Event event = new Event( eventId, value ) ;
        List<EventSubscriber> subscribers = getSubscribersForEvent( eventId ) ;
        
        if( !subscribers.isEmpty() ) {
            
            for( EventSubscriber subscriber : subscribers ) {
                
                if( printPublishLogs ) {
                    String clsName = subscriber.getClass().getName() ;
                    boolean async = false ;
                    if( subscriber instanceof AsyncEventDispatchProxy ) {
                        async = true ;
                        clsName = ((AsyncEventDispatchProxy)subscriber).getSubscriber().getClass().getName() ;
                    }
                    clsName = clsName.substring( clsName.lastIndexOf('.') + 1 ) ;
                    clsName += async ? " [Async]" : "" ;
                    
                    log.debug( "   Calling event subscriber {} for event {}", clsName, getEventName( eventId ) ) ;
                }
                subscriber.handleEvent( event ) ;
            }
        }
    }
    
    public String getEventName( final int eventType ) {
        String eventName = "EVENT_NAME_UNKNOWN" ;
        if( eventCatalogClass != null ) {
            if( eventIdVsNameMap.containsKey( eventType ) ) {
                eventName = eventIdVsNameMap.get( eventType ) ;
            }
            else {
                eventName = extractEventName( eventType ) ;
                eventIdVsNameMap.put( eventType, eventName ) ;
            }
        }
        return eventName ;
    }
    
    private synchronized List<EventSubscriber> getSubscribersForEvent( int eventId ) {
        
        List<EventSubscriber> subscribers = new ArrayList<>() ;
        if( eventIdSubscriberMap.containsKey( eventId ) ) {
            subscribers.addAll( eventIdSubscriberMap.get( eventId ) ) ;
        }
        
        List<EventSubscriber> allEventSubscribers = eventIdSubscriberMap.get( ALL_EVENTS ) ;
        if( allEventSubscribers != null ) {
            for( EventSubscriber anAllEvtSubscriber : allEventSubscribers ) {
                if( !isSubscriberPresent( subscribers, anAllEvtSubscriber ) ) {
                    subscribers.add( anAllEvtSubscriber ) ;
                }
            }
        }
        return subscribers ;
    }
    
    private String extractEventName( final int eventId ) {
        try {
            Field[] fields = eventCatalogClass.getDeclaredFields() ;
            for ( Field field : fields ) {
                if( field.getAnnotation( Payload.class ) != null ) {
                    Integer id = ( Integer )field.get( EventCatalog.class ) ;
                    if( id == eventId ) {
                        return field.getName() ;
                    }
                }
            }
        }
        catch( Exception e ) {
            log.error( "Error while getting event name for event code {}", eventId, e ) ;
        }
        return "EVENT_NAME_UNKNOWN" ;
    }
}
