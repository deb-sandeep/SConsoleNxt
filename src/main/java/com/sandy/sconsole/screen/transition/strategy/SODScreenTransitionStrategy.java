package com.sandy.sconsole.screen.transition.strategy;

import com.sandy.sconsole.initializers.ScreenManagerInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Slf4j
@Component
public class SODScreenTransitionStrategy {

    public String computeTransition( Calendar calendar ) {
        int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY ) ;
        if( hourOfDay == 10 ) {
            return ScreenManagerInitializer.REFRESHER_SCR_NAME;
        }
        return null ;
    }
}
