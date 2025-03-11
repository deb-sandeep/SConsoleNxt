package com.sandy.sconsole.screen.refresher;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.daemon.refresher.RefresherSlideManager;
import com.sandy.sconsole.screen.clock.tile.DateTile;
import com.sandy.sconsole.screen.clock.tile.TimeTile;
import com.sandy.sconsole.screen.refresher.quote.QuoteRefresherPanel;
import com.sandy.sconsole.screen.refresher.slide.SlideRefresherPanel;
import com.sandy.sconsole.screen.refresher.vocab.VocabRefresherPanel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.SConsole.getApp;

@Slf4j
@Component
public class RefresherScreen extends Screen implements ClockTickListener {

    @Autowired private ApplicationContext appCtx ;
    @Autowired private SConsoleConfig appCfg ;
    @Autowired private NVPConfigAnnotationProcessor nvpAnnotationProcessor ;
    @Autowired private RefresherSlideManager slideManager ;

    private TimeTile timeTile ;
    private DateTile dateTile ;

    private final List<AbstractRefresherPanel> refresherPanelList = new ArrayList<>() ;

    private AbstractRefresherPanel currentRefresherPanel = null ;
    private int currentRefresherPanelIndex = -1 ;
    private long currentPanelDisplayStartTime = -1 ;

    @Override
    public void initialize( UITheme theme ) {
        super.setUpBaseUI( theme ) ;
        initializeRefresherPanel( new SlideRefresherPanel( theme, appCfg, slideManager ) ) ;
        initializeRefresherPanel( new VocabRefresherPanel( theme ) ) ;
        initializeRefresherPanel( new QuoteRefresherPanel( theme ) ) ;
        setUpUI( theme ) ;
        setRefresherPanel( 0 ) ;
    }

    private void initializeRefresherPanel( AbstractRefresherPanel panel ) {

        appCtx.getBean( NVPConfigAnnotationProcessor.class )
              .processNVPConfigConsumer( panel ) ;
        nvpAnnotationProcessor.processNVPConfigConsumer( panel ) ;
        panel.initialize() ;
        refresherPanelList.add( panel ) ;
    }

    private void setUpUI( UITheme theme ) {

        dateTile = new DateTile( "dd MMM, EE", theme, 50 ) ;
        timeTile = new TimeTile( theme, 70 ) ;

        dateTile.setBorder( new EmptyBorder( 0, 50,0,0 ) );
        dateTile.setHorizontalAlignment( JLabel.LEFT ) ;

        // Top two rows are reserved for common display elements such as
        // Date, Time and Star rating of the currently displayed refresher.
        super.addTile( dateTile, 0,  0, 5,  1 ) ;
        super.addTile( timeTile, 6,  0, 9,  1 ) ;
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

        if( currentRefresherPanel != null ) {
            long currentTimeMillis = calendar.getTimeInMillis() ;
            long displayDuration = (currentTimeMillis - currentPanelDisplayStartTime) / 1000 ;

            if( displayDuration >= currentRefresherPanel.getDisplayDuration() ) {
                int nextPanelIndex = currentRefresherPanelIndex+1 ;
                if( nextPanelIndex >= refresherPanelList.size() ) {
                    nextPanelIndex = 0 ;
                }
                setRefresherPanel( nextPanelIndex ) ;
            }
            else {
                int callbackInterval = currentRefresherPanel.getCallbackInterval() ;
                if( callbackInterval > 0 ) {
                    if( displayDuration != 0 &&
                        displayDuration % callbackInterval == 0 ) {
                        log.debug( "Changing refresher panel content" ) ;
                        currentRefresherPanel.refresherScreenCallback() ;
                    }
                }
            }
        }
    }

    private void setRefresherPanel( int panelIndex ) {

        if( panelIndex != currentRefresherPanelIndex ) {
            AbstractRefresherPanel panel = refresherPanelList.get( panelIndex ) ;
            if( currentRefresherPanel != null ) {
                super.remove( currentRefresherPanel ) ;
                super.invalidate() ;
            }

            log.debug( "Setting refresher panel - {}", panel.getClass().getSimpleName() ) ;
            currentRefresherPanel = panel ;
            currentRefresherPanelIndex = panelIndex ;
        }

        currentRefresherPanel.refresh() ;
        currentPanelDisplayStartTime = System.currentTimeMillis() ;

        SwingUtilities.invokeLater( ()->{
            super.addTile( currentRefresherPanel, 0,2,15,15 ) ;
            super.revalidate() ;
            super.repaint() ;
        } ) ;
    }
}
