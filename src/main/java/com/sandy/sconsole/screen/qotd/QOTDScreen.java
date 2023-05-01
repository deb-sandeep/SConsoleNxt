package com.sandy.sconsole.screen.qotd;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.ui.Screen;
import com.sandy.sconsole.core.ui.uiutil.StringTile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.dao.quote.Quote;
import com.sandy.sconsole.screen.clock.tile.DateTile;
import com.sandy.sconsole.screen.clock.tile.TimeTile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.SConsole.getApp;
import static com.sandy.sconsole.SConsole.getAppCtx;

public class QOTDScreen extends Screen implements ClockTickListener {

    public static final int DEF_QUOTE_REFRESH_TIME_SEC = 600 ;

    private TimeTile timeTile ;
    private DateTile dateTile ;
    private StringTile quoteSectionTile ;
    private StringTile quoteTextTile ;
    private StringTile quoteAuthorTile ;

    private Quote currentQuote ;
    private Date lastLoadTime ;
    private int quoteRefreshTime = DEF_QUOTE_REFRESH_TIME_SEC;

    @Override
    public void initialize( UITheme theme ) {
        super.setUpBaseUI( theme ) ;
        setUpUI( theme ) ;
        loadNextQuote() ;
    }

    private void setUpUI( UITheme theme ) {

        timeTile = new TimeTile( this, theme, 70 ) ;
        dateTile = new DateTile( this, theme, 50 ) ;

        quoteSectionTile = new StringTile( this, theme, 50 ) ;
        quoteSectionTile.setLabelForeground( Color.CYAN.darker() ) ;

        quoteTextTile    = new StringTile( this, theme, 80, JLabel.CENTER ) ;
        quoteTextTile.setLabelFont( new Font( "Ariel", Font.PLAIN, 70 ) );
        quoteTextTile.setBorder( new EmptyBorder( 0, 50, 0, 50 ) ) ;

        quoteAuthorTile  = new StringTile( this, theme, 60, JLabel.RIGHT ) ;
        quoteAuthorTile.setLabelForeground( Color.YELLOW.darker() ) ;
        quoteAuthorTile.setBorder( new EmptyBorder( 0, 0, 0, 50 ) ) ;


        super.addTile( dateTile,         0,  0, 5,  1 ) ;
        super.addTile( timeTile,         6,  0, 9,  1 ) ;
        super.addTile( quoteSectionTile, 10, 0, 15, 1 ) ;
        super.addTile( quoteTextTile,    0,  2, 15, 13 ) ;
        super.addTile( quoteAuthorTile,  0,  14,15, 15 ) ;
    }

    @Override
    public void beforeActivation() {
        getApp().getClock().addTickListener( this, TimeUnit.SECONDS ) ;
    }

    @Override
    public void beforeDeactivation() {
        getApp().getClock().removeTickListener( this ) ;
    }

    @Override
    public void clockTick( Calendar calendar ) {
        timeTile.updateDisplay( calendar ) ;
        dateTile.updateDisplay( calendar ) ;

        if( lastLoadTime != null ) {
            long elapsedMillis = calendar.getTime().getTime() - lastLoadTime.getTime() ;
            if( elapsedMillis > quoteRefreshTime* 1000L ) {
                loadNextQuote() ;
            }
        }
    }

    public void setQuoteRefreshTime( int sec ) {
        this.quoteRefreshTime = sec ;
    }

    private void loadNextQuote() {

        QOTDManager qotdManager = getAppCtx().getBean( QOTDManager.class ) ;
        Quote nextQuote = qotdManager.getNextRandomQuote() ;
        while( nextQuote == this.currentQuote ) {
            nextQuote = qotdManager.getNextRandomQuote() ;
        }

        this.currentQuote = nextQuote ;
        this.lastLoadTime = new Date() ;

        SwingUtilities.invokeLater( () ->{
            quoteSectionTile.setLabelText( currentQuote.getSection() ) ;
            quoteTextTile.setLabelHTMLText( currentQuote.getQuote() ) ;
            quoteAuthorTile.setLabelText( "- " + currentQuote.getSpeaker() ) ;

            quoteTextTile.setLabelForeground( SwingUtils.getRandomColor() ) ;
        } ) ;
    }
}
