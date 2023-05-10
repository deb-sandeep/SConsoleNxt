package com.sandy.sconsole.screen.refresher.vocab;

import com.sandy.sconsole.core.ui.screen.util.StringTile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.dao.word.Word;
import com.sandy.sconsole.dao.word.WordRepo;
import com.sandy.sconsole.screen.refresher.AbstractRefresherPanel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Random;

import static com.sandy.sconsole.SConsole.getAppCtx;

@Slf4j
public class VocabRefresherPanel extends AbstractRefresherPanel {

    private StringTile wordTile ;
    private StringTile meaningTile ;
    private StringTile exampleTile ;

    private Word currentWord ;

    public VocabRefresherPanel( UITheme uiTheme ) {
        super( uiTheme );
    }

    @Override
    public void initialize() {
        setUpUI() ;
    }

    @Override
    public void refresh() {
        loadNextWord() ;
    }

    private void setUpUI() {

        wordTile = new StringTile( theme, 80, JLabel.CENTER, JLabel.BOTTOM ) ;
        wordTile.setLabelForeground( Color.CYAN.brighter() ) ;

        meaningTile = new StringTile( theme, 55, JLabel.CENTER ) ;
        meaningTile.setLabelFont( new Font( "Roboto", Font.PLAIN, 55 ) );
        meaningTile.setLabelForeground( Color.CYAN.darker().darker().darker() ) ;
        meaningTile.setBorder( new EmptyBorder( 0, 50, 0, 50 ) ) ;

        exampleTile = new StringTile( theme, 40, JLabel.CENTER ) ;
        exampleTile.setLabelFont( new Font( "Ariel", Font.ITALIC, 40 ) );
        exampleTile.setLabelForeground( Color.GRAY ) ;
        exampleTile.setBorder( new EmptyBorder( 0, 50, 0, 50 ) ) ;

        super.addTile( wordTile, 0,0,15,3 ) ;
        super.addTile( meaningTile, 0,4,15,9 ) ;
        super.addTile( exampleTile, 0,10,15,15 ) ;
    }

    private void loadNextWord() {

        currentWord = getNextWord() ;

        final Word finalWord = currentWord ;
        SwingUtilities.invokeLater( () ->{
            wordTile.setLabelText( StringUtils.capitalize( finalWord.getWord() ) ) ;
            wordTile.setLabelForeground( SwingUtils.getRandomColor() ) ;

            meaningTile.setLabelHTMLText( StringUtils.capitalize( finalWord.getMeaning() ) ) ;

            String example = finalWord.getExample() ;
            example = example == null ? "" : example ;
            example = StringUtils.capitalize( example ) ;
            exampleTile.setLabelHTMLText( example ) ;
        } ) ;
    }

    private Word getNextWord() {

        WordRepo wordRepo = getAppCtx().getBean( WordRepo.class ) ;
        List<Word> words = wordRepo.findTop100ByExampleIsNotNullOrderByFrequencyDesc() ;
        Word word = words.get( new Random().nextInt( words.size() )  ) ;

        if( currentWord != null ) {
        while( word.getId().equals( currentWord.getId() ) ) {
                word = words.get( new Random().nextInt( words.size() )  ) ;
            }
        }
        word.setNumShows( word.getNumShows()+1 ) ;
        wordRepo.save( word ) ;

        return word ;
    }
}
