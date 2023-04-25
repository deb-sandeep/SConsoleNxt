package com.sandy.sconsole.test.core.clock.mock;

import com.sandy.sconsole.core.clock.ClockTickListener;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

public class MockClockTickListener implements ClockTickListener {

    @Getter @Setter
    private int numTicksRecd = 0 ;

    @Override
    public void clockTick( Calendar calendar ) {
        numTicksRecd++ ;
    }
}
