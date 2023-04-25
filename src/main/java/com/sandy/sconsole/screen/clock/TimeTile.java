package com.sandy.sconsole.screen.clock;

import com.sandy.sconsole.core.ui.Screen;
import com.sandy.sconsole.core.ui.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeTile extends Tile {

    private static final SimpleDateFormat SDF = new SimpleDateFormat( "HH:mm:ss" ) ;
    private JLabel timeLabel ;

    public TimeTile( Screen parent, UITheme theme ) {
        super( parent, theme, false ) ;
        setUpUI( theme ) ;
    }

    private void setUpUI( UITheme theme ) {
        timeLabel = super.getTemplateLabel() ;
        timeLabel.setFont( theme.getLabelFont( 150 ) ) ;
        super.add( timeLabel, BorderLayout.CENTER ) ;
    }

    public void updateDisplay( Calendar calendar ) {
        timeLabel.setText( SDF.format( calendar.getTime() ) ) ;
    }
}
