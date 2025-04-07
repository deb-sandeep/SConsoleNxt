package com.sandy.sconsole.poc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class DatePoC {
    
    private static final SimpleDateFormat DF = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ;
    
    public static void main( String[] args ) throws ParseException {
        Date d1 = DF.parse( "2025-04-04 10:00:00" ) ;
        Date d2 = DF.parse( "2025-04-07 12:00:00" ) ;
        
        log.debug( "d1 = {}", DF.format( d1 ) ) ;
        log.debug( "d2 = {}", DF.format( d2 ) ) ;
        
//        Date d = truncateTime( d1 ) ;
//        log.debug( "Truncated d1 = {}", DF.format( d ) ) ;
//
//        Date nd = nextDay( d ) ;
//        log.debug( "Next day to d1 = {}", DF.format( nd ) ) ;
        
        log.debug( "Duration between d1 and d2 = {}", durationDays( d1, d2 ) );
    }
    
    private static Date truncateTime( Date d ) {
        return DateUtils.truncate( d, Calendar.DAY_OF_MONTH ) ;
    }
    
    private static Date nextDay( Date d ) {
        return DateUtils.addDays( d, 1 ) ;
    }
    
    private static int durationDays( Date d1, Date d2 ) {
        Date startDay = truncateTime( d1 ) ;
        Date endDay = nextDay( truncateTime( d2 ) ) ;
        
        return (int)(( endDay.getTime() - startDay.getTime() )/DateUtils.MILLIS_PER_DAY) ;
    }
}
