package com.sandy.sconsole.screen.autotransition.strategy;

import com.sandy.sconsole.initializer.ScreenManagerInitializer;
import com.sandy.sconsole.screen.autotransition.AutoScreenTransitionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Slf4j
@Component
/**
 * This Start of Day strategy keeps instigating a switch to the QOTD screen
 * between 7 AM and 8 AM. At other times it does not recommend a transition.
 */
public class SODScreenTransitionStrategy extends AutoScreenTransitionStrategy {

    public SODScreenTransitionStrategy() {
        super( "SOD Screen Transition Strategy" ) ;
    }

    @Override
    public String computeTransition( Calendar calendar ) {
        int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY ) ;
        if( hourOfDay == 10 ) {
            return ScreenManagerInitializer.QOTD_SCR_NAME ;
        }
        return null ;
    }
}
