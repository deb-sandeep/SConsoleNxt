package com.sandy.sconsole.initializers;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.ScreenBuilder;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.core.ui.screen.screens.dock.DockScreen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.screens.clock.ClockScreen;
import com.sandy.sconsole.screen.screens.refresher.RefresherScreen;
import org.springframework.stereotype.Component;

@Component
public class ScreenManagerInitializer implements ComponentInitializer {

    public static final String DOCK_SCR_NAME  = "Dock" ;
    public static final String CLOCK_SCR_NAME = "Clock" ;
    public static final String REFRESHER_SCR_NAME = "Refresher" ;

    private UITheme theme = null ;
    private ScreenManager screenManager = null ;

    @Override
    public int getInitializationSequencePreference() {
        // We want the Screen Manager to initialize later in the cycle,
        // especially after some screens have initialized themselves.
        return 100 ;
    }

    @Override
    public void initialize( SConsole app ) throws Exception {

        this.theme = app.getTheme() ;
        this.screenManager = app.getCtx().getBean( ScreenManager.class ) ;

        screenManager.registerScreen( buildDockScreen() ) ;
        screenManager.registerScreen( buildClockScreen() ) ;
        screenManager.registerScreen( buildRefresherScreen() ) ;
    }

    private Screen buildDockScreen() throws Exception {
        return ScreenBuilder.instance( theme )
                .withName( DOCK_SCR_NAME )
                .withScreenClass( DockScreen.class )
                .build() ;
    }

    private Screen buildClockScreen() throws Exception {
        return ScreenBuilder.instance( theme )
                .withName( CLOCK_SCR_NAME )
                .withScreenClass( ClockScreen.class )
                //.withShowOnStartup()
                .withParentScreen( screenManager.getScreen( DOCK_SCR_NAME ) )
                .build() ;
    }

    private Screen buildRefresherScreen() throws Exception {
        return ScreenBuilder.instance( theme )
                .withName( REFRESHER_SCR_NAME )
                .withScreenClass( RefresherScreen.class )
                .withShowOnStartup()
                .withParentScreen( screenManager.getScreen( DOCK_SCR_NAME ) )
                .build() ;
    }
}
