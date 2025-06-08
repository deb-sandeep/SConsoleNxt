package com.sandy.sconsole.core.bus;

import com.sandy.sconsole.EventCatalog;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EventUtils {
    
    private static final Class<?> EVT_CATALOG_CLASS = EventCatalog.class ;
    
    private static final Map<Integer, String> eventIdVsNameMap = new HashMap<>() ;
    
    public static String getEventName( final int eventId ) {
        String eventName = "EVENT_NAME_UNKNOWN (" + eventId + ")" ;
        if( EVT_CATALOG_CLASS != null ) {
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
    
    private static String extractEventName( final int eventId ) {
        try {
            Field[] fields = EVT_CATALOG_CLASS.getDeclaredFields() ;
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
    
}
