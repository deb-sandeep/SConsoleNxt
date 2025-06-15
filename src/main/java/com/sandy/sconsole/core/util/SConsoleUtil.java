package com.sandy.sconsole.core.util;

import com.sandy.sconsole.SConsole;
import org.apache.commons.lang3.time.DateUtils;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

public class SConsoleUtil {
    
    public static boolean isOperatingOnPiMon() {
        return SConsole.getApp()
                       .getConfig()
                       .getEnvType()
                       .equalsIgnoreCase( "PROD" ) ;
    }
    
    public static boolean isBetween( Date start, Date end, Date date ) {
        return date.after( start ) && date.before( end ) ;
    }
    
    public static int duration( Date start, Date end ) {
        return (int)Duration.between( start.toInstant(), end.toInstant() ).toDays() ;
    }
    
    private static Date truncateTime( Date d ) {
        return DateUtils.truncate( d, Calendar.DAY_OF_MONTH ) ;
    }
    
    public static Date nextDay( Date d ) {
        return DateUtils.addDays( d, 1 ) ;
    }
    
    // Returns the number of full days between the start of d1 (00:00:00)
    // and the start of day after d2.
    // Example:
    //    d1 = 2025-04-04 00:00:00
    //    d2 = 2025-04-07 00:00:00
    //    durationDays( d1, d2 ) = 4
    public static int durationDays( Date d1, Date d2 ) {
        Date startDay = truncateTime( d1 ) ;
        Date endDay = nextDay( truncateTime( d2 ) ) ;
        
        return (int)(( endDay.getTime() - startDay.getTime() )/DateUtils.MILLIS_PER_DAY) ;
    }
}
