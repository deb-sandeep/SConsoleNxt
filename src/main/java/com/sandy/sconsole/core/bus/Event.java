package com.sandy.sconsole.core.bus;

import lombok.Getter;

@Getter
public class Event {

    private final int eventId;
    private final Object value ;
    private final long eventTime ;

    public Event( final int eventId, final Object value ) {
        this.eventId = eventId ;
        this.value = value ;
        this.eventTime = System.currentTimeMillis() ;
    }
}
