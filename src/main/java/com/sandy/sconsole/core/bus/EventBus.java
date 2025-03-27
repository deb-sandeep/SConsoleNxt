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
    
    private final Map<Integer, List<EventSubscriber>> subscriberMap = new HashMap<>() ;
    private final ExecutorService executor = Executors.newFixedThreadPool( 10 ) ;
    
    // A class which contains public static final int definitions of events
    // This class is introspected to reverse translate the event code to
    // event name for debugging purposes
    @Setter private Class<?> eventCatalogClass ;
    
    @Setter private boolean printPublishLogs = false ;
    
    private final Map<Integer, String> eventCodeVsNameMap = new HashMap<>() ;
    
    /**
     * Register a subscriber with a variable number of interested event types.
     * The added subscriber will be notified if an event is generated for
     * any of the interested event types.
     *
     * @param subscriber The subscriber instance to register.
     *
     * @param asyncDispatch A boolean flag indicating if the subscriber prefers
     *        to receive the events in the same thread as the publisher or
     *        asynchronously.
     *
     * @param eventTypes The interested event types for which this subscriber
     *        will be notified by the bus. If the event types is null,
     *        this subscriber will be notified on all the events.
     */
    public synchronized void addSubscriber( final EventSubscriber subscriber,
                                            final boolean asyncDispatch,
                                            final int... eventTypes ) {
        
        if( eventTypes == null || eventTypes.length == 0 ) {
            addSubscriberToEventMap( subscriber, asyncDispatch, ALL_EVENTS ) ;
        }
        else {
            for( final int type : eventTypes ) {
                addSubscriberToEventMap( subscriber, asyncDispatch, type ) ;
            }
        }
    }
    
    /**
     * Removes the specified subscriber from the provided event types. Once this
     * method is called, notifications to the subscriber will not be sent for
     * the event types for which the subscriber is being removed.
     *
     * @param subscriber The subscriber instance to de-register.
     *
     * @param eventTypes The event types for which this subscriber
     *        will not be notified by the bus. If the event types is null,
     *        this subscriber will be removed from all existing registrations
     */
    public synchronized void removeSubscriber( final EventSubscriber subscriber,
                                               int... eventTypes ) {
        
        if( eventTypes == null || eventTypes.length == 0 ) {
            removeSubscriberFromEventMap( subscriber, ALL_EVENTS ) ;
            for( Integer eventId : subscriberMap.keySet() ) {
                removeSubscriberFromEventMap( subscriber, eventId ) ;
            }
        }
        else {
            for( final int type : eventTypes ) {
                removeSubscriberFromEventMap( subscriber, type ) ;
            }
        }
    }
    
    public synchronized void publishEvent( final int eventType, final Object value ) {
        
        if( printPublishLogs ) {
            log.debug( "Publishing event {}", getEventName( eventType ) ) ;
        }
        
        Event event = new Event( eventType, value ) ;
        List<EventSubscriber> subscribers = getSubscribersForEvent( eventType ) ;
        
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
                    
                    log.debug( "   Calling event subscriber {} for event {}", clsName, getEventName( eventType ) ) ;
                }
                
                subscriber.handleEvent( event ) ;
            }
        }
    }
    
    public synchronized void publishEvent( final int eventType ) {
        publishEvent( eventType, null ) ;
    }
    
    public void terminateExecutor() {
        this.executor.shutdown() ;
    }
    
    public String getEventName( final int eventType ) {
        String eventName = "EVENT_NAME_UNKNOWN" ;
        if( eventCatalogClass != null ) {
            if( eventCodeVsNameMap.containsKey( eventType ) ) {
                eventName = eventCodeVsNameMap.get( eventType ) ;
            }
            else {
                eventName = extractEventName( eventType ) ;
                eventCodeVsNameMap.put( eventType, eventName ) ;
            }
        }
        return eventName ;
    }
    
    private void addSubscriberToEventMap( EventSubscriber subscriber,
                                          boolean asyncDispatch, int event ) {
     
        List<EventSubscriber> subscribers = subscriberMap.computeIfAbsent( event, k -> new ArrayList<>() ) ;
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
                if( proxy.getSubscriber().equals( subscriber ) ) {
                    return true ;
                }
            }
            else if( s.equals( subscriber ) ) {
                return true ;
            }
        }
        return false ;
    }
    
    private void removeSubscriberFromEventMap( EventSubscriber subscriber, int event ) {
        
        AsyncEventDispatchProxy asyncProxy ;
        
        List<EventSubscriber> subscribers = subscriberMap.get( event ) ;
        if( subscribers != null ) {
            for( Iterator<EventSubscriber> esIter = subscribers.iterator(); esIter.hasNext(); ) {
                
                EventSubscriber regSubscriber = esIter.next() ;
                
                if( regSubscriber instanceof AsyncEventDispatchProxy ) {
                    regSubscriber = (( AsyncEventDispatchProxy )regSubscriber).getSubscriber() ;
                }
                
                if( regSubscriber.equals( subscriber ) ) {
                    esIter.remove() ;
                }
            }
        }
    }

    private synchronized List<EventSubscriber> getSubscribersForEvent( int event ) {
        
        List<EventSubscriber> subscribers = new ArrayList<>() ;
        if( subscriberMap.containsKey( event ) ) {
            subscribers.addAll( subscriberMap.get( event ) ) ;
        }
        
        List<EventSubscriber> allEventSubscribers = subscriberMap.get( ALL_EVENTS ) ;
        if( allEventSubscribers != null ) {
            for( EventSubscriber anAllEvtSubscriber : allEventSubscribers ) {
                if( !isSubscriberPresent( subscribers, anAllEvtSubscriber ) ) {
                    subscribers.add( anAllEvtSubscriber ) ;
                }
            }
        }
        return subscribers ;
    }
    
    private String extractEventName( final int eventCode ) {
        try {
            Field[] fields = eventCatalogClass.getDeclaredFields() ;
            for ( Field field : fields ) {
                if( field.getAnnotation( Payload.class ) != null ) {
                    Integer code = ( Integer )field.get( EventCatalog.class ) ;
                    if( code == eventCode ) {
                        return field.getName() ;
                    }
                }
            }
        }
        catch( Exception e ) {
            log.error( "Error while getting event name for event code {}", eventCode, e ) ;
        }
        return "EVENT_NAME_UNKNOWN" ;
    }
}
