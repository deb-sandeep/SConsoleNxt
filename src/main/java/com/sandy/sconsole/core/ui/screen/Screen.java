package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.core.ui.screen.util.AbstractPanel;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Screen extends AbstractPanel {

    private String name ;
    private String icon ;
    
    protected Screen() {}

    /**
     * This method needs to be called on the screen post creation to delegate
     * the screen initialization to this instance. This method should be
     * overridden by the concrete subclasses. It is a best practice to call
     * <code>super.initialize()</code> before the subclasses start their
     * own processing.
     */
    public abstract void initialize( UITheme theme ) ;

    protected void setUpBaseUI( UITheme theme ) {
        super.setBackground( theme.getBackgroundColor() ) ;
        super.setDefaultTableLayout() ;
    }
    
    /** This method is called before a screen is made visible. */
    public void beforeActivation() {}

    /** This method is called before a screen is removed from display. */
    public void beforeDeactivation() {}

    public String toString() {
        return this.name ;
    }
}
