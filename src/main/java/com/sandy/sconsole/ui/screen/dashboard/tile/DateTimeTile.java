package com.sandy.sconsole.ui.screen.dashboard.tile;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.ui.screen.Tile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Component
@Scope( "prototype" )
public class DateTimeTile extends Tile implements ClockTickListener {

    private static final Font TIME_FONT     = new Font( "Courier", Font.PLAIN, 100 ) ;
    private static final Font DATE_DAY_FONT = new Font( "Courier", Font.PLAIN, 45 ) ;
    
    private static final SimpleDateFormat TIME_SDF = new SimpleDateFormat( "HH:mm:ss", Locale.ENGLISH ) ;
    private static final SimpleDateFormat DATE_SDF = new SimpleDateFormat( "dd MMMM",  Locale.ENGLISH ) ;
    private static final SimpleDateFormat DAY_SDF  = new SimpleDateFormat( "EEEE",     Locale.ENGLISH ) ;

    private JLabel timeLabel = null ;
    private JLabel dateLabel = null ;
    private JLabel dayLabel  = null ;
    
    @Autowired private SConsoleClock clock ;
    
    public DateTimeTile() {}
    
    @Override
    public void initialize() {
        timeLabel = super.createEmptyLabel() ;
        timeLabel.setFont( TIME_FONT ) ;
        
        dateLabel = super.createEmptyLabel() ;
        dateLabel.setHorizontalAlignment( SwingConstants.LEFT ) ;
        dateLabel.setFont( DATE_DAY_FONT ) ;
        
        dayLabel = super.createEmptyLabel() ;
        dateLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        dayLabel.setFont( DATE_DAY_FONT ) ;
        
        add( timeLabel, BorderLayout.CENTER ) ;
        add( dateLabel, BorderLayout.WEST   ) ;
        add( dayLabel,  BorderLayout.EAST   ) ;
    }

    @Override
    public void beforeActivation() {
        clock.addTickListener( this, TimeUnit.SECONDS, TimeUnit.DAYS ) ;
        
        Calendar now = Calendar.getInstance() ;
        dayTicked( now ) ;
        secondTicked( now ) ;
    }
    
    @Override
    public void beforeDeactivation() {
        clock.removeTickListener( this ) ;
    }
    
    @Override
    public void dayTicked( Calendar time ) {
        Date date = time.getTime() ;
        dateLabel.setText( DATE_SDF.format( date ) ) ;
        dayLabel.setText( DAY_SDF.format( date ) ) ;
    }
    
    @Override
    public void secondTicked( Calendar time ) {
        timeLabel.setText( TIME_SDF.format( time.getTime() ) ) ;
    }
}
