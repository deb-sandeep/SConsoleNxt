package com.sandy.sconsole.test.core.clock.mock;

import com.sandy.sconsole.core.clock.SConsoleClock;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class MockCurrentTimeProvider extends SConsoleClock.CurrentTimeProvider {

    private int callCount = 0 ;
    @Override
    public Calendar getCurrentTime() {
        callCount++ ;
        if( callCount < 2 ) {
            return super.getCurrentTime() ;
        }
        Calendar nextDay = Calendar.getInstance() ;
        nextDay.setTime( DateUtils.addDays( new Date(), 1 ) ) ;
        return nextDay ;
    }
}
