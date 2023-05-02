package com.sandy.sconsole.screen.autotransition;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

public abstract class AutoScreenTransitionStrategy {

    // The more the weight, the higher the precedence.
    @Getter @Setter
    private int weight ;

    @Getter
    private final String name ;

    protected AutoScreenTransitionStrategy( String name ) {
        this( 0, name ) ;
    }

    protected AutoScreenTransitionStrategy( int weight, String name ) {
        this.weight = weight ;
        this.name = name ;
    }

    /**
     * If an implementation of this function returns a null, it implies that
     * there is no recommendation for transition. If multiple strategies
     * are registered then the strategies will get invoked in decreasing
     * order of their weights. The first non-null transition will be given
     * precedence.
     *
     * @param calendar The date at which the daemon is calling this strategy.
     *
     * @return The name of the screen to be displayed. It is assumed that
     * a screen by this name is registered with the ScreenManager.
     */
    public abstract String computeTransition( Calendar calendar ) ;
}
