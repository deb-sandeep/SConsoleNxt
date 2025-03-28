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
    
    /**
     * Returns true if the time provided lies between start and end of this day.
     * startTime inclusive and endTime exclusive.
     */
    public boolean contains( Date time ) {
        return startTime.getTime() <= time.getTime() &&
               endTime.getTime() > time.getTime() ;
    }
    
    /** Returns true if this day ends before the given date */
    public boolean endsBefore( Date time ) {
        return endTime.compareTo( time ) < 0 ;
    }
    
    /** Returns true if this day starts after the given date */
    public boolean startsAfter( Date time ) {
        return startTime.compareTo( time ) >= 0 ;
    }
}
