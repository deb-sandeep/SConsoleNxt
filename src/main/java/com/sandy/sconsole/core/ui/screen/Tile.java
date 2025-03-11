package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.core.ui.screen.util.AbstractPanel;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

import javax.swing.*;
import java.awt.*;

public abstract class Tile extends AbstractPanel {

    protected UITheme theme ;
    
    protected Tile( UITheme theme ) {
        this( theme, false ) ;
    }

    protected Tile( UITheme theme, boolean isBordered ) {
        this( theme, false, -1, -1 ) ;
    }
    
    protected Tile( UITheme theme, boolean isBordered, int numRows, int numCols ) {
        this.theme = theme ;
        setUpUI( theme, isBordered, numRows, numCols ) ;
    }

    private void setUpUI( UITheme theme, boolean isBordered, int numRows, int numCols ) {
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
}
