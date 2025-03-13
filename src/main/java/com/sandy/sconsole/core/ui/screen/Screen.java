package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.core.ui.screen.util.AbstractPanel;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Screen extends AbstractPanel {
    
    private static final int DEF_EPHEMERAL_LIFE_SPAN = 60 ; // In seconds

    private String id ;
    private String screenName;
    private int    priority = 0 ;
    private boolean ephemeral = false ;
    private int ephemeralLifeSpan = -1 ;
    
    protected Screen( String id, String screenName ) {
        this.id = id ;
        this.screenName = screenName;
        this.withLifeSpan( DEF_EPHEMERAL_LIFE_SPAN ) ;
    }
    
    public Screen withPriority( int priority ) {
        this.priority = priority ;
        return this ;
    }
    
    public Screen asPerpetual() {
        this.ephemeral = false ;
        this.ephemeralLifeSpan = -1 ;
        return this ;
    }
    
    public Screen withLifeSpan( int lifeSpan ) {
        this.ephemeral = true ;
        this.ephemeralLifeSpan = lifeSpan ;
        return this ;
    }

    /**
     * This method needs to be called on the screen post creation to delegate
     * the screen initialization to this instance. This method should be
     * overridden by the concrete subclasses. It is a best practice to call
     * <code>super.initialize()</code> before the subclasses start their
     * own processing.
     */
    public abstract void initialize() ;

    protected void setUpBaseUI( UITheme theme ) {
        super.setBackground( theme.getBackgroundColor() ) ;
        super.setDefaultTableLayout() ;
    }
    
    /** This method is called before a screen is made visible. */
    public void beforeActivation() {}

    /** This method is called before a screen is removed from display. */
    public void beforeDeactivation() {}
}
