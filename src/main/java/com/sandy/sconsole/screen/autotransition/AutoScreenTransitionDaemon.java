package com.sandy.sconsole.screen.autotransition;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.screen.autotransition.strategy.EODScreenTransitionStrategy;
import com.sandy.sconsole.screen.autotransition.strategy.SODScreenTransitionStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This daemon is invoked by the SConsole system clock every second. Upon
 * invocation, it loops through the registered strategies in decreasing
 * order of their weights and on encountering a transition request, instructs
 * the main frame to replace the current screen with the next requested screen.
 */
@Slf4j
@Component
public class AutoScreenTransitionDaemon implements ClockTickListener {

    private SConsole app ;
    private ScreenManager scrMgr ;
    private List<AutoScreenTransitionStrategy> strategies = new ArrayList<>() ;

    @Autowired
    public void setSConsole( SConsole app ) { this.app = app ; }

    @Autowired
    public void setScreenManager( ScreenManager mgr ) { this.scrMgr = mgr ; }

    @PostConstruct
    public void initialize() {
        app.getClock().addTickListener( this, TimeUnit.MINUTES ) ;
        strategies.add( app.getCtx().getBean( EODScreenTransitionStrategy.class ) ) ;
        strategies.add( app.getCtx().getBean( SODScreenTransitionStrategy.class ) ) ;
    }

    @PreDestroy
    public void destroy() {
        app.getClock().removeTickListener( this ) ;
    }

    public void addAutoScreenTransitionStrategy( AutoScreenTransitionStrategy s ) {
        this.strategies.add( s ) ;
        strategies.sort( Comparator.comparingInt( AutoScreenTransitionStrategy::getWeight ).reversed() ) ;
    }

    @Override
    public void clockTick( Calendar calendar ) {

        // The clock starts before the frame is made visible, so any ticks
        // received before the frame is initialized will return null. Hence
        // the check.
        if( app.getFrame() == null ) return ;

        Screen currentScreen = app.getFrame().getCurrentScreen() ;

        if( currentScreen != null && currentScreen.isReplaceableByAutoScreenTransitionDaemon() ) {

            for( AutoScreenTransitionStrategy sts : strategies ) {
                String nextScreenName = sts.computeTransition( calendar ) ;
                if( nextScreenName != null && !currentScreen.getName().equals( nextScreenName )) {
                    app.getFrame().changeScreen( nextScreenName ) ;
                    return ;
                }
            }
        }
    }
}
