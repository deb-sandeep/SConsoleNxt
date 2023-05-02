package com.sandy.sconsole.screen.autotransition.strategy;

import com.sandy.sconsole.screen.screens.ScreenManagerInitializer;
import com.sandy.sconsole.screen.autotransition.AutoScreenTransitionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Slf4j
@Component
/**
 * This End of Day strategy keeps instigating a switch to the Clock screen
 * after 11 PM and till 7 AM. Between 7 AM and 11 PM, this strategy does
 * not recommend a transition.
 */
public class EODScreenTransitionStrategy extends AutoScreenTransitionStrategy {

    public EODScreenTransitionStrategy() {
        super( "EOD Screen Transition Strategy" ) ;
    }

    @Override
    public String computeTransition( Calendar calendar ) {
        int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY ) ;
        if( hourOfDay == 23 || hourOfDay < 7 ) {
            return ScreenManagerInitializer.CLOCK_SCR_NAME ;
        }
        return null ;
    }
}
