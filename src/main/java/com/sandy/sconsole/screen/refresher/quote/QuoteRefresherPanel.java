package com.sandy.sconsole.screen.refresher.quote;

import com.sandy.sconsole.core.ui.screen.tiles.StringTile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.dao.quote.Quote;
import com.sandy.sconsole.dao.quote.QuoteManager;
import com.sandy.sconsole.screen.refresher.AbstractRefresherPanel;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.sandy.sconsole.SConsole.getAppCtx;

@Slf4j
public class QuoteRefresherPanel extends AbstractRefresherPanel {

    private StringTile quoteSectionTile ;
    private StringTile quoteTextTile ;
    private StringTile quoteAuthorTile ;

    private Quote currentQuote ;

    public QuoteRefresherPanel( UITheme uiTheme ) {
        super( uiTheme );
    }

    @Override
    public void initialize() {
        setUpUI() ;
    }

    @Override
    public void refresh() {
        loadNextQuote() ;
    }

    private void setUpUI() {

        quoteSectionTile = new StringTile( theme, 50, JLabel.CENTER, JLabel.BOTTOM ) ;
        quoteSectionTile.setLabelFont( new Font( "Ariel", Font.ITALIC, 50 ) );
        quoteSectionTile.setLabelForeground( Color.GRAY ) ;

        quoteTextTile = new StringTile( theme, 80, JLabel.CENTER ) ;
        quoteTextTile.setLabelFont( new Font( "Roboto", Font.PLAIN, 70 ) );
        quoteTextTile.setBorder( new EmptyBorder( 0, 50, 0, 50 ) ) ;

        quoteAuthorTile = new StringTile( theme, 60, JLabel.RIGHT ) ;
        quoteAuthorTile.setLabelForeground( Color.YELLOW.darker() ) ;
        quoteAuthorTile.setBorder( new EmptyBorder( 0, 0, 0, 50 ) ) ;

        super.addTile( quoteSectionTile, 0,0,15,2 ) ;
        super.addTile( quoteTextTile, 0,3,15,12 ) ;
        super.addTile( quoteAuthorTile, 0,13,15,15 ) ;
    }

    private void loadNextQuote() {

        QuoteManager quoteManager = getAppCtx().getBean( QuoteManager.class ) ;
        Quote nextQuote = quoteManager.getNextRandomQuote() ;
        while( nextQuote == this.currentQuote ) {
            nextQuote = quoteManager.getNextRandomQuote() ;
        }
        quoteManager.incrementViewCount( nextQuote ) ;
        this.currentQuote = nextQuote ;

        SwingUtilities.invokeLater( () ->{
            quoteSectionTile.setLabelText( currentQuote.getSection() ) ;
            quoteTextTile.setLabelHTMLText( currentQuote.getQuote() ) ;
            quoteTextTile.setLabelForeground( SwingUtils.getRandomColor().darker() ) ;
            quoteAuthorTile.setLabelText( "- " + currentQuote.getSpeaker() ) ;
        } ) ;
    }
}
