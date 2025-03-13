package com.sandy.sconsole.core.clock;

import java.util.Calendar;

public interface ClockTickListener {

    default void dayTicked( Calendar calendar ) {}
    
    default void hourTicked( Calendar calendar ) {}
    
    default void minuteTicked( Calendar calendar ) {}
    
    default void secondTicked( Calendar calendar ) {}

    default boolean isAsync() {
        return false ;
    }
}
