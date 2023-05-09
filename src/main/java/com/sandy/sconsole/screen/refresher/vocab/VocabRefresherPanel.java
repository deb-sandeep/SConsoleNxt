package com.sandy.sconsole.screen.refresher.vocab;

import com.sandy.sconsole.core.ui.screen.util.StringTile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.dao.word.Word;
import com.sandy.sconsole.dao.word.WordRepo;
import com.sandy.sconsole.screen.refresher.AbstractRefresherPanel;
import lombok.extern.slf4j.Slf4j;

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

        EmptyBorder border = new EmptyBorder( 0, 50, 0, 50 ) ;

        wordTile = new StringTile( theme, 80, JLabel.CENTER, JLabel.BOTTOM ) ;
        wordTile.setLabelForeground( Color.CYAN.brighter() ) ;

        meaningTile = new StringTile( theme, 60, JLabel.CENTER ) ;
        meaningTile.setLabelFont( new Font( "Roboto", Font.PLAIN, 60 ) );
        meaningTile.setLabelForeground( Color.GREEN.darker() ) ;
        meaningTile.setBorder( border ) ;

        exampleTile = new StringTile( theme, 50, JLabel.RIGHT, JLabel.TOP ) ;
        exampleTile.setLabelForeground( Color.YELLOW.darker() ) ;
        exampleTile.setBorder( border ) ;

        super.addTile( wordTile, 0,0,15,2 ) ;
        super.addTile( meaningTile, 0,3,15,9 ) ;
        super.addTile( exampleTile, 0,10,15,15 ) ;
    }

    private void loadNextWord() {

        currentWord = getNextWord() ;

        final Word finalWord = currentWord ;
        SwingUtilities.invokeLater( () ->{
            wordTile.setLabelText( finalWord.getWord() ) ;
            wordTile.setLabelForeground( SwingUtils.getRandomColor().darker() ) ;
            meaningTile.setLabelHTMLText( finalWord.getMeaning() ) ;
            exampleTile.setLabelText( finalWord.getMeaning() ) ;
        } ) ;
    }

    private Word getNextWord() {

        WordRepo wordRepo = getAppCtx().getBean( WordRepo.class ) ;
        List<Word> words = wordRepo.findTop100ByOrderByFrequencyDescNumShowsAsc() ;
        Word word = words.get( new Random().nextInt( words.size() )  ) ;

        while( word.getId().equals( currentWord.getId() ) ) {
            word = words.get( new Random().nextInt( words.size() )  ) ;
        }
        word.setNumShows( word.getNumShows()+1 ) ;
        wordRepo.save( word ) ;

        return word ;
    }
}
