package com.sandy.sconsole.initializer;

import com.sandy.sconsole.core.ui.Screen;
import com.sandy.sconsole.core.ui.ScreenBuilder;
import com.sandy.sconsole.core.ui.ScreenManager;
import com.sandy.sconsole.core.ui.screen.dock.DockScreen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.clock.ClockScreen;

public class ScreenManagerInitializer {

    public static final String DOCK_SCR_NAME  = "Dock" ;
    public static final String CLOCK_SCR_NAME = "Clock" ;

    private UITheme theme = null ;
    private ScreenManager screenManager = null ;

    public ScreenManager initialize( UITheme theme ) throws Exception {

        this.theme = theme ;
        this.screenManager = new ScreenManager() ;

        screenManager.registerScreen( buildDockScreen() ) ;
        screenManager.registerScreen( buildClockScreen() ) ;

        return screenManager ;
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
                .withShowOnStartup()
                .withParentScreen( screenManager.getScreen( DOCK_SCR_NAME ) )
                .build() ;
    }
}
