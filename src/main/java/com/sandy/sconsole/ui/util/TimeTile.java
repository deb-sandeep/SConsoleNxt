package com.sandy.sconsole.ui.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeTile extends AbstractCalendarTile {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "HH:mm:ss" ) ;
    
    public TimeTile( int fontSize ) {
        super( fontSize, "HH:mm:ss" ) ;
    }
    
    @Override
    public void secondTicked( Calendar calendar ) {
        super.updateDisplay( calendar ) ;
    }
}
