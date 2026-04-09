package com.sandy.sconsole.ui.screen.session.tile;

import com.sandy.sconsole.state.manager.ProblemStateCounter;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static com.sandy.sconsole.core.ui.uiutil.UITheme.BG_COLOR;
import static com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterTile.*;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.RIGHT;

public class ProblemStateCounterRowPanel extends JPanel {

    private final JLabel[] countLabels = new JLabel[COUNTER_VALUE_PROVIDERS.length] ;

    public ProblemStateCounterRowPanel( String scopeLabel, int topBorderWidth ) {
        setUpUI( scopeLabel, topBorderWidth ) ;
    }
    
    private void setUpUI( String scopeLabel, int topBorderWidth ) {
        
        configureRowLayout( this ) ;
        add( createCellLabel( scopeLabel, HEADER_FONT, topBorderWidth ), "0,0" ) ;
        
        for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
            JLabel label = createCellLabel( "", VALUE_FONT, topBorderWidth ) ;
            countLabels[i] = label ;
            add( label, ( i + 1 ) + ",0" ) ;
        }
    }
    
    public static JPanel createHeaderPanel() {

        JPanel panel = createBasePanel() ;
        for( int i=0; i<COLUMN_HEADERS.length; i++ ) {
            panel.add( createCellLabel( COLUMN_HEADERS[i], HEADER_FONT, 1 ), i + ",0" ) ;
        }
        return panel ;
    }

    public void setCounter( ProblemStateCounter counter ) {
        for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
            if( counter == null ) {
                countLabels[i].setText( "" ) ;
                countLabels[i].setForeground( HDR_FG_COLOR ) ;
            }
            else {
                int value = COUNTER_VALUE_PROVIDERS[i].getValue( counter ) ;
                countLabels[i].setText( String.valueOf( value ) ) ;
                countLabels[i].setForeground( value == 0 ? HDR_FG_COLOR : COLUMN_VALUE_COLORS[i] ) ;
            }
        }
    }

    private static JPanel createBasePanel() {
        JPanel panel = new JPanel() ;
        configureRowLayout( panel ) ;
        return panel ;
    }

    private static void configureRowLayout( JPanel panel ) {

        panel.setBackground( BG_COLOR ) ;

        TableLayout layout = new TableLayout() ;
        layout.insertRow( 0, TableLayout.FILL ) ;
        for( int i=0; i<NUM_COUNTER_COLUMNS; i++ ) {
            layout.insertColumn( i, COUNTER_COL_WIDTH ) ;
        }
        panel.setLayout( layout ) ;
    }

    private static JLabel createCellLabel( String text,
                                           Font font,
                                           int topBorderWidth ) {

        JLabel label = new JLabel( text ) ;
        label.setHorizontalAlignment( RIGHT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( HDR_FG_COLOR ) ;
        label.setBackground( BG_COLOR ) ;
        label.setFont( font ) ;
        label.setBorder(
            BorderFactory.createCompoundBorder(
                new MatteBorder( topBorderWidth, 1, 1, 1, GRID_COLOR ),
                BorderFactory.createEmptyBorder( 0, 0, 0, COUNTER_CELL_RIGHT_INSET )
            )
        ) ;
        return label ;
    }
}
