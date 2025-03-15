package com.sandy.sconsole.core.util;

import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

@Getter
public class Day {

    private final Date startTime ;
    private final Date endTime ;
    
    public Day() {
        this( new Date() ) ;
    }
    
    public Day( Date date ) {
        this.startTime = DateUtils.truncate( date, Calendar.DAY_OF_MONTH ) ;
        this.endTime = DateUtils.addSeconds( this.startTime, 86400 ) ;
    }
    
    /** Returns true if this day ends before the given date */
    public boolean before( Date time ) {
        return endTime.compareTo( time ) <= 0 ;
    }
    
    /** Returns true if this day starts after the given date */
    public boolean after( Date time ) {
        return startTime.after( time ) ;
    }
    
    public boolean contains( Date time ) {
        return time.compareTo( startTime ) >= 0 &&
               time.before( endTime ) ;
    }
}
