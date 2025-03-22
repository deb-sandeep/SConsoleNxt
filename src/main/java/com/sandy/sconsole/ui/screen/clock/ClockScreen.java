package com.sandy.sconsole.ui.screen.clock;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.util.DateTile;
import com.sandy.sconsole.ui.util.TimeTile;
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
        setUpUI() ;
    }

    private void setUpUI() {
        timeTile = new TimeTile( 150 ) ;
        dateTile = new DateTile( 70 ) ;
        super.addTile( timeTile, 2, 10, 13, 18 ) ;
        super.addTile( dateTile, 2, 18, 13, 21 ) ;
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
