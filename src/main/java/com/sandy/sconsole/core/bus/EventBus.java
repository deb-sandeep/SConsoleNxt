package com.sandy.sconsole.core.bus;

import com.sandy.sconsole.EventCatalog;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
@Component( "eventBus" )
public class EventBus {

    public static final int ALL_EVENTS = 0xCAFEBABE ;
    
    public static class EventRange {
        
        private final int lowerBoundEventId ;
        private final int upperBoundEventId ;
        
        EventRange( int lowerEventId, int upperEventId ) {
            this.lowerBoundEventId = lowerEventId ;
            this.upperBoundEventId = upperEventId ;
        }
        
        boolean containsEventId( int eventId ) {
            return ( eventId >= lowerBoundEventId ) && 
                   ( eventId <= upperBoundEventId ) ;
        }

        public boolean equals( Object o ) {

            if( o instanceof EventRange r ) {
                return ( r.lowerBoundEventId == lowerBoundEventId ) &&
                       ( r.upperBoundEventId == upperBoundEventId ) ;
            }
            return false ;
        }
    }

    private final Map<Integer, List<EventSubscriber>> eventSubscriberMap =
            new HashMap<>() ;
    
    private final Map<EventRange, List<EventSubscriber>> eventRangeSubscriberMap =
            new HashMap<>() ;
    
    // A class which contains public static final int definitions of events
    // This class is introspected to reverse translate the event code to
    // event name for debugging purposes
    @Setter private Class<?> eventCatalogClass ;
    
    @Setter private boolean printPublishLogs = false ;
    
    private final Map<Integer, String> eventCodeVsNameMap = new HashMap<>() ;
    
    private boolean isSubscriberPresent( List<EventSubscriber> subscribers, 
                                         EventSubscriber subscriber ) {
        
        for( EventSubscriber aSubscriberInList : subscribers ) {
            if( aSubscriberInList instanceof AsyncEventDispatchProxy ) {
                AsyncEventDispatchProxy proxy ;
                proxy = ( AsyncEventDispatchProxy )aSubscriberInList ;
                if( proxy.getSubscriber().equals( subscriber ) ) {
                    return true ;
                }
            }
            else if( aSubscriberInList.equals( subscriber ) ) {
                return true ;
            }
        }
        return false ;
    }
    
    private void addSubscriberToEventMap( 
            EventSubscriber subscriber, boolean asyncDispatch, int event ) {
     
        List<EventSubscriber> subscribers ;

        subscribers = eventSubscriberMap.computeIfAbsent( event, k -> new ArrayList<>() ) ;

        if( !isSubscriberPresent( subscribers, subscriber ) ) {
             if( asyncDispatch ) {
                 subscribers.add( new AsyncEventDispatchProxy( subscriber ) ) ;
             }
             else {
                 subscribers.add( subscriber ) ;
             }
         }
     }
    
    private void removeSubscriberFromEventMap(
                                       EventSubscriber subscriber, int event ) {
        
        List<EventSubscriber> subscribers ;
        EventSubscriber       regSubscriber ;
        AsyncEventDispatchProxy asyncProxy ;
        
        subscribers = eventSubscriberMap.get( event ) ;
        if( subscribers != null ) {
            for( Iterator<EventSubscriber> esIter = subscribers.iterator(); 
                 esIter.hasNext(); ) {
                
                regSubscriber = esIter.next() ;
                if( regSubscriber.equals( subscriber ) ) {
                    esIter.remove() ;
                    if( regSubscriber instanceof AsyncEventDispatchProxy ) {
                        asyncProxy = ( AsyncEventDispatchProxy )regSubscriber ;
                        asyncProxy.stop() ;
                    }
                }
            }
        }
    }

    /**
     * Returns a list of all registered events for the given subscriber. Note 
     * that a subscriber can be registered for a specific event and also for
     * all events. In such a case the returned list will have both specific and
     * ALL_EVENTS id.
     */
    public synchronized List<Integer> getRegisteredEventsForSubscriber( 
                                                  EventSubscriber subscriber ) {
        
        List<Integer> registeredEvents = new ArrayList<>() ;
        for( Map.Entry<Integer, List<EventSubscriber>> entry : 
             eventSubscriberMap.entrySet() ) {
            
            if( isSubscriberPresent( entry.getValue(), subscriber ) ) {
                registeredEvents.add( entry.getKey() ) ;
            }
        }
        return registeredEvents ;
    }
    
    public synchronized List<EventRange> getRegisteredEventRangesForSubscriber(
                                                EventSubscriber subscriber ) {
        
        List<EventRange> registeredEventRanges = new ArrayList<>() ;
        for( Map.Entry<EventRange, List<EventSubscriber>> entry : 
             eventRangeSubscriberMap.entrySet() ) {
            
            if( isSubscriberPresent( entry.getValue(), subscriber ) ) {
                registeredEventRanges.add( entry.getKey() ) ;
            }
        }
        return registeredEventRanges ;
    }
    
    public synchronized List<EventSubscriber> getSubscribersForEvent( int event ) {
        
        List<EventSubscriber> subscribers = new ArrayList<>() ;
        if( eventSubscriberMap.containsKey( event ) ) {
            subscribers.addAll( eventSubscriberMap.get( event ) ) ;
        }
        
        List<EventSubscriber> allEventSubscribers = eventSubscriberMap.get( ALL_EVENTS ) ;
        if( allEventSubscribers != null ) {
            for( EventSubscriber anAllEvtSubscriber : allEventSubscribers ) {
                if( !isSubscriberPresent( subscribers, anAllEvtSubscriber ) ) {
                    subscribers.add( anAllEvtSubscriber ) ;
                }
            }
        }
        
        for( Map.Entry<EventRange, List<EventSubscriber>> entry : 
            eventRangeSubscriberMap.entrySet() ) {
            
            if( entry.getKey().containsEventId( event ) ) {
                for( EventSubscriber aRangeSubscriber : entry.getValue() ) {
                    if( !isSubscriberPresent( subscribers, aRangeSubscriber ) ) {
                        subscribers.add( aRangeSubscriber ) ;
                    }
                }
            }
        }
        return subscribers ;
    }
    
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
    public synchronized void addSubscriberForEventTypes( 
                                            final EventSubscriber subscriber,
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
    
    public synchronized void addSubscriberForEventRange( 
                                            final EventSubscriber subscriber,
                                            final boolean asyncDispatch,
                                            final int lowerRangeEventId,
                                            final int upperRangeEventId ) {
        
        EventRange range = new EventRange( lowerRangeEventId, upperRangeEventId ) ;
        if( eventRangeSubscriberMap.containsKey( range ) ) {
            eventRangeSubscriberMap.get( range ).add( subscriber ) ;
        }
        else {
            List<EventSubscriber> subscribers = new ArrayList<>() ;
            subscribers.add( subscriber ) ;
            eventRangeSubscriberMap.put( range, subscribers ) ;
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
    public synchronized void removeSubscriber( 
                                              final EventSubscriber subscriber,
                                              int... eventTypes ) {
        
        if( eventTypes == null || eventTypes.length == 0 ) {
            removeSubscriberFromEventMap( subscriber, ALL_EVENTS ) ;
            for( Integer eventId : eventSubscriberMap.keySet() ) {
                removeSubscriberFromEventMap( subscriber, eventId ) ;
                
                for( Entry<EventRange, List<EventSubscriber>> entry : 
                     eventRangeSubscriberMap.entrySet() ) {
                    
                    if( isSubscriberPresent( entry.getValue(), subscriber ) ) {
                        entry.getValue().remove( subscriber ) ;
                    }
                }
            }
        }
        else {
            for( final int type : eventTypes ) {
                removeSubscriberFromEventMap( subscriber, type ) ;
                
                for( Entry<EventRange, List<EventSubscriber>> entry : 
                    eventRangeSubscriberMap.entrySet() ) {
                   
                   if( entry.getKey().containsEventId( type ) ) {
                       if( isSubscriberPresent( entry.getValue(), subscriber ) ) {
                           entry.getValue().remove( subscriber ) ;
                       }
                   }
               }
            }
        }
    }

    /** Removes all the subscribers and attempts to stop them gracefully. */
    public synchronized void clear() {

        AsyncEventDispatchProxy proxy ;

        for( Map.Entry<Integer, List<EventSubscriber>> entry : 
             eventSubscriberMap .entrySet() ) {
            
            for( EventSubscriber subscriber : entry.getValue() ) {

                if( subscriber instanceof AsyncEventDispatchProxy ) {
                    proxy = (AsyncEventDispatchProxy) subscriber ;
                    proxy.stop() ;
                }
            }
        }

        for( Map.Entry<EventRange, List<EventSubscriber>> entry : 
             eventRangeSubscriberMap.entrySet() ) {
            
            for( EventSubscriber subscriber : entry.getValue() ) {
                if( subscriber instanceof AsyncEventDispatchProxy ) {
                    proxy = (AsyncEventDispatchProxy) subscriber ;
                    proxy.stop() ;
                }
            }
        }
        eventSubscriberMap.clear() ;
        eventRangeSubscriberMap.clear() ;
    }
    
    /**
     * Publishes an event. All the subscribers registered to the given event
     * type are notified of the event. The notification happens either
     * synchronously or asynchronously depending upon the way the subscriber
     * was added.
     *
     * @param eventType The type of event being publishes.
     *
     * @param value The value associated with this event.
     */
    public synchronized void publishEvent( final int eventType, final Object value ) {
        
        if( printPublishLogs ) {
            log.debug( "Publishing event {} with value {}", getEventName( eventType ), value ) ;
        }
        
        Event event = new Event( eventType, value ) ;
        List<EventSubscriber> subscribers = getSubscribersForEvent( eventType ) ;
        if( !subscribers.isEmpty() ) {
            for( EventSubscriber aSubscriber : subscribers ) {
                aSubscriber.handleEvent( event ) ;
            }
        }
    }
    
    public synchronized void publishEvent( final int eventType ) {
        publishEvent( eventType, null ) ;
    }
    
    private String getEventName( final int eventType ) {
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
