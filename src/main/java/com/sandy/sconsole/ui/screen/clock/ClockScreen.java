package com.sandy.sconsole.ui.screen.clock;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.screen.clock.tile.DateTile;
import com.sandy.sconsole.ui.screen.clock.tile.TimeTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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
        timeTile = new TimeTile( 150 ) ;
        dateTile = new DateTile( 70 ) ;
        super.addTile( timeTile, 2, 5, 13, 8 ) ;
        super.addTile( dateTile, 2, 9, 13, 10 ) ;
    }

    @Override
    public void beforeActivation() {
        clock.addTickListener( this, TimeUnit.SECONDS ) ;
    }

    @Override
    public void beforeDeactivation() {
        clock.removeTickListener( this ) ;
    }

    @Override
    public void clockTick( Calendar calendar ) {
        timeTile.updateDisplay( calendar ) ;
        dateTile.updateDisplay( calendar ) ;
    }
}
