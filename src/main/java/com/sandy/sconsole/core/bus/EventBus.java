package com.sandy.sconsole.core.bus;

import com.sandy.sconsole.EventCatalog;
import lombok.Getter;
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
    
    public static final int DEFAULT_PRIORITY  = 100 ;
    public static final int HIGH_PRIORITY = 200 ;
    public static final int LOW_PRIORITY  = 50 ;
    
    public static final int ASYNC_PRIORITY = 0 ;
    
    private final Map<Integer, List<EventSubscriber>> eventIdSubscriberMap = new HashMap<>() ;
    private final ExecutorService executor = Executors.newFixedThreadPool( 10 ) ;
    
    // A class which contains public static final int definitions of events
    // This class is introspected to reversely translate the event code to
    // event name for debugging purposes
    @Setter private Class<?> eventCatalogClass = EventCatalog.class ;
    
    @Setter private boolean printPublishLogs = true ;
    @Getter private final ArrayList<Integer> printPublishIgnoredEventIds = new ArrayList<>() ;
    @Getter private final ArrayList<Integer> printPublishAcceptedEventIds = new ArrayList<>() ;
    
    private final Map<Integer, String> eventIdVsNameMap = new HashMap<>() ;
    
    public void terminateExecutor() {
        this.executor.shutdown() ;
    }
    
    public synchronized void addSyncSubscriber( int priority,
                                                final EventSubscriber subscriber,
                                                final int ...eventIds ) {
        addSubscriber( subscriber, priority, false, eventIds ) ;
    }
    
    public synchronized void addSyncSubscriber( final EventSubscriber subscriber,
                                                final int ...eventIds ) {
        addSubscriber( subscriber, DEFAULT_PRIORITY, false, eventIds ) ;
    }
    
    public synchronized void addAsyncSubscriber( final EventSubscriber subscriber,
                                                 final int ...eventIds ) {
        addSubscriber( subscriber, ASYNC_PRIORITY, true, eventIds ) ;
    }
    
    private synchronized void addSubscriber( final EventSubscriber subscriber,
                                             int priority,
                                             final boolean asyncDispatch,
                                             final int ...eventIds ) {
        
        if( eventIds == null || eventIds.length == 0 ) {
            addSubscriberToEventMap( subscriber, priority, asyncDispatch, ALL_EVENTS ) ;
        }
        else {
            for( final int id : eventIds ) {
                addSubscriberToEventMap( subscriber, priority, asyncDispatch, id ) ;
            }
        }
    }
    
    private void addSubscriberToEventMap( EventSubscriber subscriber,
                                          int priority,
                                          boolean asyncDispatch, int eventId ) {
        
        List<EventSubscriber> subscribers = eventIdSubscriberMap.computeIfAbsent( eventId, k -> new ArrayList<>() ) ;
        if( !isSubscriberPresent( subscribers, subscriber ) ) {
            if( asyncDispatch ) {
                subscribers.add( new AsyncEventDispatchProxy( subscriber, executor, this ) ) ;
            }
            else {
                subscribers.add( new SyncEventDispatchProxy( subscriber, priority ) ) ;
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
        
        if( printPublishLogs && qualifiesPrintPublish( eventId )) {
            log.debug( "Publishing event {}", getEventName( eventId ) ) ;
        }
        
        Event event = new Event( eventId, value ) ;
        List<EventSubscriber> subscribers = getSubscribersForEvent( eventId ) ;
        
        if( !subscribers.isEmpty() ) {
            
            for( EventSubscriber subscriber : subscribers ) {
                
                if( printPublishLogs && qualifiesPrintPublish( eventId )) {
                    String clsName = getSubscriberName( subscriber );
                    log.debug( "   Calling event subscriber {} for event {}", clsName, getEventName( eventId ) ) ;
                }
                subscriber.handleEvent( event ) ;
            }
        }
    }
    
    private static String getSubscriberName( EventSubscriber subscriber ) {
        
        String clsName ;
        String subscriberId ;
        boolean async = false ;
        
        if( subscriber instanceof AsyncEventDispatchProxy proxy ) {
            async = true ;
            EventSubscriber rootSubscriber = proxy.getSubscriber() ;
            
            clsName = rootSubscriber.getClass().getName() ;
            subscriberId = rootSubscriber.getId() ;
        }
        else if( subscriber instanceof SyncEventDispatchProxy proxy ) {
            EventSubscriber rootSubscriber = proxy.getSubscriber() ;
            
            clsName = rootSubscriber.getClass().getName() ;
            subscriberId = rootSubscriber.getId() ;
        }
        else {
            clsName = subscriber.getClass().getSimpleName() ;
            subscriberId = subscriber.getId() ;
        }
        
        clsName = clsName.substring( clsName.lastIndexOf('.') + 1 ) ;
        clsName = (async ? "(~) " : "(*) ") + clsName ;
        
        if( !subscriberId.isBlank() ) {
            clsName += "( " + subscriberId + " )" ;
        }
        
        return clsName;
    }
    
    public String getEventName( final int eventId ) {
        String eventName = "EVENT_NAME_UNKNOWN (" + eventId + ")" ;
        if( eventCatalogClass != null ) {
            if( eventIdVsNameMap.containsKey( eventId ) ) {
                eventName = eventIdVsNameMap.get( eventId ) ;
            }
            else {
                eventName = extractEventName( eventId ) ;
                eventIdVsNameMap.put( eventId, eventName ) ;
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
        
        subscribers.sort( ( e1, e2 ) -> getPriority( e2 ) - getPriority( e1 ) ) ;
        
        return subscribers ;
    }
    
    private int getPriority( EventSubscriber subscriber ) {
        if( subscriber instanceof AsyncEventDispatchProxy ) {
            return ASYNC_PRIORITY ;
        }
        else if( subscriber instanceof SyncEventDispatchProxy proxy ) {
            return proxy.getPriority() ;
        }
        return DEFAULT_PRIORITY;
    }
    
    private String extractEventName( final int eventId ) {
        try {
            Field[] fields = eventCatalogClass.getDeclaredFields() ;
            for ( Field field : fields ) {
                if( field.getAnnotation( PayloadType.class ) != null ) {
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
        return "EVENT_NAME_UNKNOWN (" + eventId + ")" ;
    }
    
    private boolean qualifiesPrintPublish( int eventId ) {
        if( !printPublishIgnoredEventIds.isEmpty() &&
            printPublishIgnoredEventIds.contains( eventId ) ) {
            return false ;
        }
        else if( !printPublishAcceptedEventIds.isEmpty() &&
                 printPublishAcceptedEventIds.contains( eventId ) ) {
            return true ;
        }
        return true ;
    }
}
