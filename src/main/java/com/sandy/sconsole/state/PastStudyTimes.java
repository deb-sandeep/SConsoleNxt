package com.sandy.sconsole.state;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class PastStudyTimes {

    private int numPastDays ;
    
    protected Date today ;
    protected Date startDate ;
    protected Map<Date, Integer> studyTimes = new HashMap<>() ;
    
    protected PastStudyTimes( int numPastDays ) {
        this.numPastDays = numPastDays ;
    }
    
    public void init() {
        today = DateUtils.truncate( new Date(), Calendar.DAY_OF_MONTH ) ;
        startDate = DateUtils.addDays( today, -29 ) ;
        
        studyTimes.clear() ;
        studyTimes.put( startDate, 0 ) ;
        for( int i=1; i<30; i++ ) {
            Date date = DateUtils.addDays( startDate, i ) ;
            studyTimes.put( date, 0 ) ;
        }

        getPastStudyTimes( this.numPastDays ).forEach( (key, value) -> {
            if( studyTimes.containsKey( key ) ) {
                studyTimes.put( key, value ) ;
            }
        } ) ;
    }
    
    public final void updateTodayTime() {
        studyTimes.put( today, getTodayTime() ) ;
    }
    
    protected abstract Map<Date, Integer> getPastStudyTimes( int numPastDays ) ;
    
    protected abstract int getTodayTime() ;
    
    /** The subclasses should override, call super method and then do their processing. */
    protected void clearState() { studyTimes.clear() ;}
}
