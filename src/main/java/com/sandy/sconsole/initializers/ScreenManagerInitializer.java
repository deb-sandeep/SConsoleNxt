package com.sandy.sconsole.initializers;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.ScreenBuilder;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.core.ui.screen.screens.dock.DockScreen;
import com.sandy.sconsole.screen.clock.ClockScreen;
import com.sandy.sconsole.screen.refresher.RefresherScreen;
import org.springframework.stereotype.Component;

@Component
public class ScreenManagerInitializer implements ComponentInitializer {

    public static final String DOCK_SCR_NAME  = "Dock" ;
    public static final String CLOCK_SCR_NAME = "Clock" ;
    public static final String REFRESHER_SCR_NAME = "Refresher" ;

    private ScreenManager screenManager = null ;

    @Override
    public int getInitializationSequencePreference() {
        // We want the Screen Manager to initialize later in the cycle,
        // especially after some screens have initialized themselves.
        return 100 ;
    }

    @Override
    public void initialize( SConsole app ) throws Exception {

        this.screenManager = app.getCtx().getBean( ScreenManager.class ) ;

        screenManager.registerScreen( buildDockScreen( app ) ) ;
        screenManager.registerScreen( buildClockScreen( app ) ) ;
        screenManager.registerScreen( buildRefresherScreen( app ) ) ;
    }

    private Screen buildDockScreen( SConsole app ) throws Exception {
        return ScreenBuilder.instance( app )
                .withName( DOCK_SCR_NAME )
                .withScreenClass( DockScreen.class )
                .build() ;
    }

    private Screen buildClockScreen( SConsole app ) throws Exception {
        return ScreenBuilder.instance( app )
                .withName( CLOCK_SCR_NAME )
                .withScreenClass( ClockScreen.class )
                //.withShowOnStartup()
                .withParentScreen( screenManager.getScreen( DOCK_SCR_NAME ) )
                .build() ;
    }

    private Screen buildRefresherScreen( SConsole app ) throws Exception {
        return ScreenBuilder.instance( app )
                .withName( REFRESHER_SCR_NAME )
                .withScreenClass( RefresherScreen.class )
                .withShowOnStartup()
                .withParentScreen( screenManager.getScreen( DOCK_SCR_NAME ) )
                .build() ;
    }
}
