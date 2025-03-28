package com.sandy.sconsole.ui.util;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.ui.screen.tiles.StringTile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AbstractCalendarTile extends StringTile
    implements ClockTickListener {
    
    private final SConsoleClock clock ;

    private final SimpleDateFormat dateFormat ;
    
    protected AbstractCalendarTile( int fontSize, String dateFormatString ) {
        super( fontSize ) ;
        this.dateFormat = new SimpleDateFormat( dateFormatString) ;
        this.clock = SConsole.getBean( SConsoleClock.class ) ;
    }
    
    @Override
    public void beforeActivation() {
        clock.addTickListener( this, TimeUnit.SECONDS, TimeUnit.DAYS ) ;
        updateDisplay( Calendar.getInstance() ) ;
    }
    
    @Override
    public void beforeDeactivation() {
        clock.removeTickListener( this ) ;
    }
    
    public void updateDisplay( Calendar calendar ) {
        setLabelText( dateFormat.format( calendar.getTime() ) ) ;
    }
}
