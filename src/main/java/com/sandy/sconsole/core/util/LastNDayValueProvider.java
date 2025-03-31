package com.sandy.sconsole.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

@Slf4j
public abstract class LastNDayValueProvider
        implements DayValueProvider {

    private final int numPastDays ;
    
    protected Date today ;
    protected Date startDate ;
    protected Map<Date, DayValue> dayValueMap = new LinkedHashMap<>() ;
    
    protected LastNDayValueProvider( int numPastDays ) {
        this.numPastDays = numPastDays ;
    }
    
    public void fullRefresh() {
        clearState() ;
        getPastDayValues( this.numPastDays ).forEach( ( _date, value ) -> {
            Date date = new Date( _date.getTime() ) ;
            if( dayValueMap.containsKey( date ) ) {
                dayValueMap.put( date, new DayValue( date, value ) ) ;
            }
        } ) ;
    }
    
    public final void updateTodayValue() {
        dayValueMap.put( today, new DayValue( today, getTodayValue() ) ) ;
    }
    
    public void clearState() {
        dayValueMap.clear() ;
        today = DateUtils.truncate( new Date(), Calendar.DAY_OF_MONTH ) ;
        startDate = DateUtils.addDays( today, 1-numPastDays ) ;
        
        dayValueMap.put( startDate, new DayValue( startDate, 0 ) ) ;
        
        for( int i=1; i<numPastDays; i++ ) {
            Date date = DateUtils.addDays( startDate, i ) ;
            dayValueMap.put( date, new DayValue( date, 0 ) ) ;
        }
    }

    @Override
    public Collection<DayValue> getDayValues() {
        return dayValueMap.values() ;
    }
    
    protected abstract Map<Date, Double> getPastDayValues( int numPastDays ) ;
    
    protected abstract double getTodayValue() ;
}
