package com.sandy.sconsole.core.ui;

import com.sandy.sconsole.core.remote.KeyProcessor;
import com.sandy.sconsole.core.remote.KeySet;
import com.sandy.sconsole.core.remote.RemoteKeyEvent;
import com.sandy.sconsole.core.ui.uiutil.DebugTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import info.clearthought.layout.TableLayout;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Screen extends JPanel implements KeyProcessor {

    public static final int NUM_ROWS = 16 ;
    public static final int NUM_COLS = 16 ;

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
        setLayout() ;
    }

    private void setLayout() {

        float rowHeightPct = 1.0F/NUM_ROWS ;
        float colHeightPct = 1.0F/NUM_COLS ;

        TableLayout layout = new TableLayout() ;

        for( int i=0; i<NUM_ROWS; i++ ) {
            layout.insertRow( i, rowHeightPct ) ;
        }
        for( int i=0; i<NUM_COLS; i++ ) {
            layout.insertColumn( i, colHeightPct ) ;
        }
        setLayout( layout ) ;
    }

    public void setParentScreen( Screen screen ) {
        this.parentScreen = screen ;
        if( this.parentScreen != null ) {
            this.parentScreen.registerChildScreen( this ) ;
        }
    }

    public void fillGrid( UITheme theme ) {
        for( int i=0; i<Screen.NUM_ROWS; i++ ) {
            for( int j=0; j<Screen.NUM_COLS; j++ ) {
                Tile tile = new DebugTile( this, theme.getBackgroundColor() ) ;
                addTile( tile, i, j, i, j );
            }
        }
    }

    protected void addTile( Tile tile, int ltX, int ltY, int rbX, int rbY ) {
        super.add( tile, String.format( "%d,%d,%d,%d", ltX, ltY, rbX, rbY ) ) ;
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

    public void handleRemoteKeyEvent( RemoteKeyEvent event ) {}
}
