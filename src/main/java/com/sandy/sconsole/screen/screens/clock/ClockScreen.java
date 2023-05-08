package com.sandy.sconsole.screen.screens.clock;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.screens.clock.tile.DateTile;
import com.sandy.sconsole.screen.screens.clock.tile.TimeTile;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.SConsole.getApp;

@Component
public class ClockScreen extends Screen implements ClockTickListener {

    private TimeTile timeTile ;
    private DateTile dateTile ;

    @Override
    public void initialize( UITheme theme ) {
        super.setUpBaseUI( theme ) ;
        setUpUI( theme ) ;
    }

    private void setUpUI( UITheme theme ) {
        timeTile = new TimeTile( this, theme, 150 ) ;
        dateTile = new DateTile( this, theme, 70 ) ;
        super.addTile( timeTile, 2, 5, 13, 8 ) ;
        super.addTile( dateTile, 2, 9, 13, 10 ) ;
    }

    @Override
    public void beforeActivation() {
        getApp().getClock().addTickListener( this, TimeUnit.SECONDS ) ;
    }

    @Override
    public void beforeDeactivation() {
        getApp().getClock().removeTickListener( this ) ;
    }

    @Override
    public void clockTick( Calendar calendar ) {
        timeTile.updateDisplay( calendar ) ;
        dateTile.updateDisplay( calendar ) ;
    }
}
