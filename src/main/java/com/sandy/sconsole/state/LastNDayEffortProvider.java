package com.sandy.sconsole.state;

import com.sandy.sconsole.core.util.DayValue;
import com.sandy.sconsole.core.util.DayValueProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

@Slf4j
public abstract class LastNDayEffortProvider
        implements DayValueProvider {

    private final int numPastDays ;
    
    protected Date today ;
    protected Date startDate ;
    protected Map<Date, DayValue> studyTimes = new LinkedHashMap<>() ;
    
    protected LastNDayEffortProvider( int numPastDays ) {
        this.numPastDays = numPastDays ;
    }
    
    public void fullRefresh() {
        clearState() ;
        
        today = DateUtils.truncate( new Date(), Calendar.DAY_OF_MONTH ) ;
        startDate = DateUtils.addDays( today, 1-numPastDays ) ;
        
        studyTimes.put( startDate, new DayValue( startDate, 0 ) ) ;
        for( int i=1; i<numPastDays; i++ ) {
            Date date = DateUtils.addDays( startDate, i ) ;
            studyTimes.put( date, new DayValue( date, 0 ) ) ;
        }

        getPastStudyTimes( this.numPastDays ).forEach( (_date, value ) -> {
            Date date = new Date( _date.getTime() ) ;
            if( studyTimes.containsKey( date ) ) {
                studyTimes.put( date, new DayValue( date, (float)value/3600 ) ) ;
            }
        } ) ;
    }
    
    @Override
    public Collection<DayValue> getDayValues() {
        return studyTimes.values() ;
    }
    
    public final void updateTodayTime() {
        studyTimes.put( today, new DayValue( today, (float)getTodayTime()/3600 ) ) ;
    }
    
    protected abstract Map<Date, Integer> getPastStudyTimes( int numPastDays ) ;
    
    protected abstract int getTodayTime() ;
    
    /** The subclasses should override, call super method and then do their processing. */
    public void clearState() { studyTimes.clear() ;}
}
