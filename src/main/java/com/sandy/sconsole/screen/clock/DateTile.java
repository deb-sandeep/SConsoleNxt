package com.sandy.sconsole.screen.clock;

import com.sandy.sconsole.core.ui.Screen;
import com.sandy.sconsole.core.ui.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTile extends Tile {

    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd MMMM, EEEE" ) ;
    private JLabel dateTile;

    public DateTile( Screen parent, UITheme theme ) {
        super( parent, theme, false ) ;
        setUpUI( theme ) ;
    }

    private void setUpUI( UITheme theme ) {
        dateTile = super.getTemplateLabel() ;
        dateTile.setFont( theme.getLabelFont( 40 ) ) ;
        super.add( dateTile, BorderLayout.CENTER ) ;
    }

    public void updateDisplay( Calendar calendar ) {
        dateTile.setText( SDF.format( calendar.getTime() ) ) ;
    }
}
