package com.sandy.sconsole.core.bus;

/**
 * A class encapsulating the event information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class Event {

    private final int eventType ;
    private final Object value ;
    private final long eventTime ;

    public Event( final int eventType, final Object value ) {
        this.eventType = eventType ;
        this.value = value ;
        this.eventTime = System.currentTimeMillis() ;
    }

    public int getEventType() {
        return this.eventType ;
    }

    public Object getValue() {
        return this.value ;
    }

    public long getEventTime() {
        return this.eventTime ;
    }
}
