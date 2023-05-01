package com.sandy.sconsole.test.core.clock.mock;

import com.sandy.sconsole.core.clock.SConsoleClock;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MockRolloverTimeProvider extends SConsoleClock.CurrentTimeProvider {

    private final TimeUnit rolloverUnit ;

    public MockRolloverTimeProvider( TimeUnit timeUnit ) {
        rolloverUnit = timeUnit ;
    }

    private int callCount = 0 ;
    @Override
    public Calendar getCurrentTime() {
        callCount++ ;
        if( callCount < 2 ) {
            return super.getCurrentTime() ;
        }
        Calendar nextDay = Calendar.getInstance() ;
        if( rolloverUnit == TimeUnit.DAYS ) {
            nextDay.setTime( DateUtils.addDays( new Date(), 1 ) ) ;
        }
        else if( rolloverUnit == TimeUnit.HOURS ) {
            nextDay.setTime( DateUtils.addHours( new Date(), 1 ) ) ;
        }
        return nextDay ;
    }
}
