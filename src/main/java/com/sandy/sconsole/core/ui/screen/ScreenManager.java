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

    public Screen getScreen( String name ) {
        return screenMap.get( name ) ;
    }

    public void registerScreen( Screen screen ) {
        screenMap.put( screen.getName(), screen ) ;
    }
}
