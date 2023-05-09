package com.sandy.sconsole.screen.screens.refresher;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.util.StarTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.screens.clock.tile.DateTile;
import com.sandy.sconsole.screen.screens.clock.tile.TimeTile;
import com.sandy.sconsole.screen.screens.refresher.quote.QuoteRefresherPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.SConsole.getApp;

@Component
public class RefresherScreen extends Screen implements ClockTickListener {

    @Autowired private ApplicationContext appCtx ;

    private TimeTile timeTile ;
    private DateTile dateTile ;
    private StarTile starTile ;

    private final List<RefresherPanel> refresherPanelList = new ArrayList<>() ;
    private RefresherPanel currentRefresherPanel = null ;

    @Override
    public void initialize( UITheme theme ) {
        super.setUpBaseUI( theme ) ;
        loadRefresherPanel( new QuoteRefresherPanel( this, theme ) ) ;
        setUpUI( theme ) ;
    }

    private void loadRefresherPanel( RefresherPanel panel ) {

        appCtx.getBean( NVPConfigAnnotationProcessor.class )
              .processNVPConfigConsumer( panel ) ;
        panel.initialize() ;
        refresherPanelList.add( panel ) ;
    }

    private void setUpUI( UITheme theme ) {

        dateTile = new DateTile( theme, 50 ) ;
        timeTile = new TimeTile( theme, 70 ) ;
        starTile = new StarTile( theme ) ;

        // Top two rows are reserved for common display elements such as
        // Date, Time and Star rating of the currently displayed refresher.
        super.addTile( dateTile, 0,  0, 5,  1 ) ;
        super.addTile( timeTile, 6,  0, 9,  1 ) ;
        super.addTile( starTile, 10, 0, 15, 1 ) ;

        attachRefresherPanel( refresherPanelList.get( 0 ) ) ;
    }

    private void attachRefresherPanel( RefresherPanel panel ) {
        if( currentRefresherPanel != null ) {
            super.remove( currentRefresherPanel ) ;
        }
        currentRefresherPanel = panel ;
        super.addTile( currentRefresherPanel, 0,2,15,15 ) ;
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
