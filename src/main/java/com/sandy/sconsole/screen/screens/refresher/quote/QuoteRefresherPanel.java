package com.sandy.sconsole.screen.screens.refresher.quote;

import com.sandy.sconsole.core.ui.screen.util.StringTile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.dao.quote.Quote;
import com.sandy.sconsole.dao.quote.QuoteManager;
import com.sandy.sconsole.screen.screens.refresher.RefresherPanel;
import com.sandy.sconsole.screen.screens.refresher.RefresherScreen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

import static com.sandy.sconsole.SConsole.getAppCtx;

public class QuoteRefresherPanel extends RefresherPanel {

    private StringTile quoteSectionTile ;
    private StringTile quoteTextTile ;
    private StringTile quoteAuthorTile ;

    private Quote currentQuote ;
    private Date lastLoadTime ;

    public QuoteRefresherPanel( RefresherScreen screen, UITheme uiTheme ) {
        super( screen, uiTheme );
    }

    @Override
    public void initialize() {
        super.initialize() ;
        setUpUI() ;
        loadNextQuote() ;
    }

    private void setUpUI() {

        quoteSectionTile = new StringTile( theme, 50 ) ;
        quoteSectionTile.setLabelForeground( Color.CYAN.darker() ) ;

        quoteTextTile = new StringTile( theme, 80, JLabel.CENTER ) ;
        quoteTextTile.setLabelFont( new Font( "Roboto", Font.PLAIN, 70 ) );
        quoteTextTile.setBorder( new EmptyBorder( 0, 50, 0, 50 ) ) ;

        quoteAuthorTile = new StringTile( theme, 60, JLabel.RIGHT ) ;
        quoteAuthorTile.setLabelForeground( Color.YELLOW.darker() ) ;
        quoteAuthorTile.setBorder( new EmptyBorder( 0, 0, 0, 50 ) ) ;

        super.addTile( quoteSectionTile, 0,0,15,1 ) ;
        super.addTile( quoteTextTile, 0,2,15,14 ) ;
        super.addTile( quoteAuthorTile, 0,15,15,15 ) ;
    }

    private void loadNextQuote() {

        QuoteManager qotdManager = getAppCtx().getBean( QuoteManager.class ) ;
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

            quoteTextTile.setLabelForeground( SwingUtils.getRandomColor().darker() ) ;
        } ) ;
    }
}
