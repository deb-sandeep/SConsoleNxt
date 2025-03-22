package com.sandy.sconsole.ui.util;

import com.sandy.sconsole.core.clock.ClockTickListener;

import java.util.Calendar;

public class DateTile extends AbstractCalendarTile
    implements ClockTickListener {
    
    public DateTile( int fontSize ) {
        this( fontSize, "dd MMM, EEEE" ) ;
    }
    
    public DateTile( int fontSize, String dateFmt ) {
        super( fontSize, dateFmt ) ;
    }
    
    @Override
    public void dayTicked( Calendar calendar ) {
        super.updateDisplay( calendar ) ;
    }
}
