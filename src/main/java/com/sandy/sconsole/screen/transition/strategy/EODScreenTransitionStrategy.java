package com.sandy.sconsole.screen.transition.strategy;

import com.sandy.sconsole.initializers.ScreenManagerInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Slf4j
@Component
public class EODScreenTransitionStrategy {

    public String computeTransition( Calendar calendar ) {
        int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY ) ;
        if( hourOfDay == 23 || hourOfDay < 7 ) {
            return ScreenManagerInitializer.CLOCK_SCR_NAME ;
        }
        return null ;
    }
}
