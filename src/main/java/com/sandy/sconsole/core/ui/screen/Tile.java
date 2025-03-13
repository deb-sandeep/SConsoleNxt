package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.ui.screen.util.AbstractPanel;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

import javax.swing.*;
import java.awt.*;

public abstract class Tile extends AbstractPanel {

    protected UITheme theme ;
    
    protected Tile() {
        this( false ) ;
    }

    protected Tile( boolean isBordered ) {
        this( false, -1, -1 ) ;
    }
    
    protected Tile( boolean isBordered, int numRows, int numCols ) {
        setUpUI( isBordered, numRows, numCols ) ;
    }

    private void setUpUI( boolean isBordered, int numRows, int numCols ) {
        this.theme = SConsole.getApp().getUiTheme() ;
        super.setBackground( theme.getBackgroundColor() ) ;
        if( numRows > 0 && numCols > 0 ) {
            super.setTableLayout( numRows, numCols ) ;
        }
        else {
            super.setLayout( new BorderLayout() ) ;
        }
        if( isBordered ) {
            super.setBorder( theme.getTileBorder() ) ;
        }
    }

    protected JLabel createEmptyLabel() {
        JLabel label = new JLabel() ;
        label.setHorizontalAlignment( SwingConstants.CENTER ) ;
        label.setVerticalAlignment( SwingConstants.CENTER ) ;
        label.setBackground( theme.getBackgroundColor() ) ;
        label.setForeground( theme.getTileForeground() ) ;
        label.setOpaque( true ) ;
        return label ;
    }
    
    /** This method is called before a screen is made visible. */
    public void beforeActivation() {}
    
    /** This method is called before a screen is removed from display. */
    public void beforeDeactivation() {}
}
