package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.initializer.ScreenManagerInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ScreenManager {

    private final Map<String, Screen> screenMap = new HashMap<>() ;
    private Screen defaultScreen = null ;
    private Screen dockScreen    = null ;

    public Screen getScreen( String name ) {
        return screenMap.get( name ) ;
    }

    public void registerScreen( Screen screen ) {
        screenMap.put( screen.getName(), screen ) ;
        if( screen.getName().equals( ScreenManagerInitializer.DOCK_SCR_NAME ) ) {
            dockScreen = screen ;
        }

        if( defaultScreen == null && screen.isShowOnStartup() ) {
            defaultScreen = screen ;
        }
    }

    public Screen getDefaultScreen() {
        return defaultScreen == null ? dockScreen : defaultScreen;
    }
}
