package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.ui.screen.util.AbstractPanel;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

import java.awt.*;

public abstract class Tile extends AbstractPanel {
    
    public static boolean isTile( Class<?> fieldClass ) {
        if( fieldClass == null || fieldClass.equals( Object.class ) ) {
            return false ;
        }
        else if( fieldClass.equals( Tile.class ) ) {
            return true ;
        }
        return isTile( fieldClass.getSuperclass() ) ;
    }

    protected UITheme theme ;
    
    protected Tile() {
        this( false ) ;
    }

    protected Tile( boolean isBordered ) {
        this( isBordered, -1, -1 ) ;
    }
    
    protected Tile( boolean isBordered, int numRows, int numCols ) {
        setUpUI( isBordered, numRows, numCols ) ;
    }

    private void setUpUI( boolean isBordered, int numRows, int numCols ) {
        this.theme = SConsole.getApp().getUiTheme() ;
        super.setBackground( UITheme.BG_COLOR ) ;
        if( numRows > 0 && numCols > 0 ) {
            super.setTableLayout( numRows, numCols ) ;
        }
        else {
            super.setLayout( new BorderLayout() ) ;
        }
        
        if( isBordered ) {
            super.setBorder( UITheme.TILE_BORDER ) ;
        }
    }

    /** This method is called on the tile as a part of the parent screen initialization. */
    public void initialize() {}
    
    /** This method is called before a screen is made visible. */
    public void beforeActivation() {}
    
    /** This method is called before a screen is removed from display. */
    public void beforeDeactivation() {}
}
