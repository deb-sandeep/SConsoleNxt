package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.core.remote.KeyProcessor;
import com.sandy.sconsole.core.remote.KeySet;
import com.sandy.sconsole.core.remote.RemoteKeyEvent;
import com.sandy.sconsole.core.ui.screen.util.AbstractScreenPanel;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public abstract class Screen extends AbstractScreenPanel implements KeyProcessor {

    @Getter @Setter private String name ;
    @Getter private Screen parentScreen ;
    @Getter @Setter private String icon ;
    @Getter @Setter private boolean showOnStartup ;
    @Getter private final KeySet keySet = new KeySet( false ) ;

    private final Map<String, Screen> children = new HashMap<>() ;

    protected Screen() {}

    /**
     * This method needs to be called on the screen post creation to delegate
     * the screen initialization to this instance. This method should be
     * overridden by the concrete subclasses. It is a best practice to call
     * <code>super.initialize()</code> before the subclasses start their
     * own processing.
     *
     * @param theme The UI theme with which to initialize.
     */
    public abstract void initialize( UITheme theme ) ;

    protected void setUpBaseUI( UITheme theme ) {
        super.setBackground( theme.getBackgroundColor() ) ;
        super.setDefaultTableLayout() ;
    }

    public void setParentScreen( Screen screen ) {
        this.parentScreen = screen ;
        if( this.parentScreen != null ) {
            this.parentScreen.registerChildScreen( this ) ;
        }
    }

    public void registerChildScreen( Screen screen ) {
        children.put( screen.getName(), screen ) ;
    }

    public Screen getChildScreen( String name ) {
        return children.get( name ) ;
    }

    /** This method is called before a screen is removed from view. */
    public void beforeDeactivation() {}

    /** This method is called before a screen shown. */
    public void beforeActivation() {}

    public String toString() {
        return this.name ;
    }

    @Override
    public KeySet getConsumableKeySet() {
        return KeySet.DISABLED_KEY_SET ;
    }

    @Override
    public void processKeyEvent( RemoteKeyEvent keyEvent ) {}
}
