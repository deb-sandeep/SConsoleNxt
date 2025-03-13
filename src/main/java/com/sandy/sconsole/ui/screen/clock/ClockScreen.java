package com.sandy.sconsole.ui.screen.clock;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.tiles.StringTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

class DateTile extends StringTile {
    
    private final SimpleDateFormat dateFormat ;
    
    public DateTile() {
        super( 70 ) ;
        this.dateFormat = new SimpleDateFormat( "dd MMM, EEEE" ) ;
    }
    
    public void updateDisplay( Calendar calendar ) {
        setLabelText( dateFormat.format( calendar.getTime() ) ) ;
    }
}

class TimeTile extends StringTile {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "HH:mm:ss" ) ;
    
    public TimeTile() {
        super( 150 ) ;
    }
    
    public void updateDisplay( Calendar calendar ) {
        setLabelText( SDF.format( calendar.getTime() ) ) ;
    }
}

@Component
public class ClockScreen extends Screen implements ClockTickListener {
    
    public static final String ID = "CLOCK_SCREEN" ;
    
    @Autowired private SConsoleClock clock ;
    @Autowired private UITheme theme ;

    private TimeTile timeTile ;
    private DateTile dateTile ;
    
    public ClockScreen() {
        super( ID, "Clock Screen" ) ;
    }

    @Override
    public void initialize() {
        super.setUpBaseUI( theme ) ;
        setUpUI( theme ) ;
    }

    private void setUpUI( UITheme theme ) {
        timeTile = new TimeTile() ;
        dateTile = new DateTile() ;
        super.addTile( timeTile, 2, 5, 13, 8 ) ;
        super.addTile( dateTile, 2, 9, 13, 10 ) ;
    }

    @Override
    public void beforeActivation() {
        clock.addTickListener( this, TimeUnit.SECONDS, TimeUnit.DAYS ) ;
        
        Calendar now = Calendar.getInstance() ;
        dateTile.updateDisplay( now ) ;
        timeTile.updateDisplay( now ) ;
    }

    @Override
    public void beforeDeactivation() {
        clock.removeTickListener( this ) ;
    }

    @Override
    public void secondTicked( Calendar calendar ) {
        timeTile.updateDisplay( calendar ) ;
    }
    
    @Override
    public void dayTicked( Calendar calendar ) {
        dateTile.updateDisplay( calendar ) ;
    }
}
