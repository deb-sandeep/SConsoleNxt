package com.sandy.sconsole.initializer;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.util.ScreenBuilder;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.screen.clock.ClockScreen;
import com.sandy.sconsole.screen.refresher.RefresherScreen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScreenManagerInitializer implements ComponentInitializer {

    public static final String CLOCK_SCR_NAME = "Clock" ;
    public static final String REFRESHER_SCR_NAME = "Refresher" ;
    
    @Override
    public int getInitializationSequencePreference() {
        // We want the Screen Manager to initialize later in the cycle,
        // especially after some screens have initialized themselves.
        return 100 ;
    }

    @Override
    public void initialize( SConsole app ) throws Exception {
        
        ScreenManager screenManager = app.getCtx().getBean( ScreenManager.class );

        log.debug( ">> Building screens." );
        screenManager.registerScreen( buildClockScreen( app ) ) ;
        screenManager.registerScreen( buildRefresherScreen( app ) ) ;
        log.debug( "- Building screens completed. <<" ) ;
    }

    private Screen buildClockScreen( SConsole app ) {
        log.debug( "-> Building Clock screen." ) ;
        return ScreenBuilder.instance( app )
                .withName( CLOCK_SCR_NAME )
                .withScreenClass( ClockScreen.class )
                .build() ;
    }

    private Screen buildRefresherScreen( SConsole app ) {
        log.debug( "-> Building Refresher screen." ) ;
        return ScreenBuilder.instance( app )
                .withName( REFRESHER_SCR_NAME )
                .withScreenClass( RefresherScreen.class )
                .build() ;
    }
}
