package com.sandy.sconsole.core.ui.screen.util;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.screen.tiles.DebugTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public abstract class AbstractPanel extends JPanel {

    public static final int DEFAULT_NUM_ROWS = 16 ;
    public static final int DEFAULT_NUM_COLS = 16 ;
    
    protected int numRows = -1 ;
    protected int numCols = -1 ;

    protected void setDefaultTableLayout() {
        this.setTableLayout( DEFAULT_NUM_ROWS, DEFAULT_NUM_COLS ) ;
    }
    
    protected void setTableLayout( int numRows, int numCols ) {
        
        this.numRows = numRows ;
        this.numCols = numCols ;
        
        float rowHeightPct = 1.0F/ this.numRows ;
        float colHeightPct = 1.0F/ this.numCols ;
        
        TableLayout layout = new TableLayout() ;
        
        for( int i = 0; i< this.numRows; i++ ) {
            layout.insertRow( i, rowHeightPct ) ;
        }
        for( int i = 0; i< this.numCols; i++ ) {
            layout.insertColumn( i, colHeightPct ) ;
        }
        setLayout( layout ) ;
    }

    public void fillWithDebugTiles( UITheme theme ) {
        for( int i = 0; i< Screen.DEFAULT_NUM_ROWS; i++ ) {
            for( int j = 0; j<Screen.DEFAULT_NUM_COLS; j++ ) {
                Tile tile = new DebugTile( theme.getBackgroundColor() ) ;
                addTile( tile, i, j, i, j );
            }
        }
    }

    protected void addTile( Tile tile, int ltX, int ltY, int rbX, int rbY ) {
        super.add( tile, String.format( "%d,%d,%d,%d", ltX, ltY, rbX, rbY ) ) ;
    }

    public void setDebugBorder() {
        Border existingBorder = getBorder() ;
        Border debugBorder ;

        if( existingBorder == null ) {
            debugBorder = new LineBorder( Color.GRAY ) ;
        }
        else {
            debugBorder = new CompoundBorder( new LineBorder( Color.GRAY ), existingBorder ) ;
        }
        super.setBorder( debugBorder ) ;
    }
}
