package com.sandy.sconsole.core.clock;

import java.util.Calendar;

public interface ClockTickListener {

    void clockTick( Calendar calendar ) ;

    default boolean isAsync() {
        return false ;
    }
}
