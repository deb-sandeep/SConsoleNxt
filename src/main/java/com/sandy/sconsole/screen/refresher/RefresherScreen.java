package com.sandy.sconsole.screen.refresher;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.util.StarTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.clock.tile.DateTile;
import com.sandy.sconsole.screen.clock.tile.TimeTile;
import com.sandy.sconsole.screen.refresher.quote.QuoteRefresherPanel;
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

    private final List<AbstractRefresherPanel> refresherPanelList = new ArrayList<>() ;

    private AbstractRefresherPanel currentRefresherPanel = null ;
    private int currentRefresherPanelIndex = -1 ;
    private long currentPanelDisplayStartTime = -1 ;

    @Override
    public void initialize( UITheme theme ) {
        super.setUpBaseUI( theme ) ;
        initializeRefresherPanel( new QuoteRefresherPanel( theme ) ) ;
        setUpUI( theme ) ;
    }

    private void initializeRefresherPanel( AbstractRefresherPanel panel ) {

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

        setRefresherPanel( 0 ) ;
    }

    private void setRefresherPanel( int panelIndex ) {
        if( panelIndex != currentRefresherPanelIndex ) {
            AbstractRefresherPanel panel = refresherPanelList.get( panelIndex ) ;
            if( currentRefresherPanel != null ) {
                super.remove( currentRefresherPanel ) ;
            }
            currentRefresherPanel = panel ;
            currentRefresherPanelIndex = panelIndex ;
        }
        currentRefresherPanel.refresh() ;
        super.addTile( currentRefresherPanel, 0,2,15,15 ) ;
        currentPanelDisplayStartTime = System.currentTimeMillis() ;
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

        long currentTimeMillis = calendar.getTimeInMillis() ;
        long displayDuration = (currentTimeMillis - currentPanelDisplayStartTime) / 1000 ;
        if( displayDuration >= currentRefresherPanel.getDisplayDuration() ) {
            int nextPanelIndex = currentRefresherPanelIndex++ ;
            if( nextPanelIndex >= refresherPanelList.size() ) {
                nextPanelIndex = 0 ;
            }
            setRefresherPanel( nextPanelIndex ) ;
        }
    }
}
