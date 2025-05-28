package com.sandy.sconsole.core.bus;

import lombok.Getter;

import java.util.Objects;

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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event other = (Event) obj;
        return Objects.equals( value, other.value ) &&
                Objects.equals( eventId, other.eventId ) ;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( value, eventId ) ;
    }
}
