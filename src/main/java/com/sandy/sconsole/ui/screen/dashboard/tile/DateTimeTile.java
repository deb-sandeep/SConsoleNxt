package com.sandy.sconsole.ui.screen.dashboard.tile;

import com.sandy.sconsole.core.ui.screen.Tile;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeTile extends Tile {

    private static final Font TIME_FONT = new Font( "Courier", Font.PLAIN, 180 ) ;
    private static final Font DATE_FONT = new Font( "Courier", Font.PLAIN, 80 ) ;
    
    private static final SimpleDateFormat TIME_SDF = new SimpleDateFormat( "HH:mm:ss", Locale.ENGLISH ) ;
    private static final SimpleDateFormat DATE_SDF = new SimpleDateFormat( "EEE, d MMM", Locale.ENGLISH ) ;

    private final JLabel timeLabel = new JLabel() ;
    private final JLabel dateLabel = new JLabel() ;
    
    public DateTimeTile() {
        super( false ) ;
        setUpUI() ;
    }

    private void setUpUI() {
        
        timeLabel.setHorizontalAlignment( SwingConstants.CENTER ) ;
        timeLabel.setVerticalAlignment( SwingConstants.BOTTOM ) ;
        timeLabel.setFont( TIME_FONT ) ;
        timeLabel.setForeground( Color.GRAY ) ;
        timeLabel.setText( TIME_SDF.format( new Date() ) ) ;

        dateLabel.setHorizontalAlignment( SwingConstants.CENTER ) ;
        dateLabel.setVerticalAlignment( SwingConstants.TOP ) ;
        dateLabel.setFont( DATE_FONT ) ;
        dateLabel.setForeground( Color.GRAY ) ;
        dateLabel.setText( DATE_SDF.format( new Date() ) );
        
        add( timeLabel, BorderLayout.CENTER ) ;
        add( dateLabel, BorderLayout.SOUTH ) ;
        setBorder( BorderFactory.createEmptyBorder( 0, 40, 40, 40 ) ) ;
    }

    public void dayTicked( Calendar time ) {
        dateLabel.setText( DATE_SDF.format( time.getTime() ) ) ;
    }
    
    public void secondTicked( Calendar time ) {
        timeLabel.setText( TIME_SDF.format( time.getTime() ) ) ;
    }
}
