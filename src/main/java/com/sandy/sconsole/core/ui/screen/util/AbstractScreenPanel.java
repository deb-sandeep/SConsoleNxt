package com.sandy.sconsole.core.ui.screen.util;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import info.clearthought.layout.TableLayout;

import javax.swing.*;

public class AbstractScreenPanel extends JPanel {

    public static final int NUM_ROWS = 16 ;
    public static final int NUM_COLS = 16 ;

    protected void setDefaultTableLayout() {

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

    public void fillWithDebugGrid( UITheme theme ) {
        for( int i = 0; i< Screen.NUM_ROWS; i++ ) {
            for( int j=0; j<Screen.NUM_COLS; j++ ) {
                Tile tile = new DebugTile( theme.getBackgroundColor() ) ;
                addTile( tile, i, j, i, j );
            }
        }
    }

    protected void addTile( Tile tile, int ltX, int ltY, int rbX, int rbY ) {
        super.add( tile, String.format( "%d,%d,%d,%d", ltX, ltY, rbX, rbY ) ) ;
    }
}
