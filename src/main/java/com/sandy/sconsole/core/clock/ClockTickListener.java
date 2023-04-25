package com.sandy.sconsole.core.clock;

import java.util.Calendar;

/**
 * An interface which will be notified when a specific clock tick event happens.
 */
public interface ClockTickListener {
    public void clockTick( Calendar calendar ) ;
    default public boolean isAsync() {
        return false ;
    }
}
