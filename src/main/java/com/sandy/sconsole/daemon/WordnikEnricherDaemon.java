package com.sandy.sconsole.daemon;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.daemon.DaemonBase;
import com.sandy.sconsole.core.wordnic.WordnikAdapter;
import com.sandy.sconsole.core.wordnic.WordnikWord;
import com.sandy.sconsole.dao.word.Word;
import com.sandy.sconsole.dao.word.WordRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@Slf4j
@Component
public class WordnikEnricherDaemon extends DaemonBase
    implements ComponentInitializer {

    @Autowired WordRepo wordRepo ;

    public WordnikEnricherDaemon(){
        super( "WordnikEnricherDaemon" ) ;
    }

    public void run() {
        try { Thread.sleep( 5000 ) ; } catch( InterruptedException ignore ) {}

        while( true ) {
            try {
                Word word = wordRepo.findFirstByWordnikEnrichedIsFalseOrderByFrequencyDesc() ;
                if( word == null ) {
                    log.info( "No more words left to be enriched." ) ;
                }
                else {
                    log.debug( "Enriching word {} >", word.getWord() ) ;
                    word = enrichWord( word ) ;
                    if( word != null ) {
                        log.debug( "  Enriched with {} meanings and {} examples.",
                                word.getMeanings().size(),
                                word.getExamples().size() );
                    }
                    else {
                        log.debug( "  Skipped enriching word." ) ;
                    }
                }
            }
            catch( Exception e ) {
                log.error( "- WordnikEnricherDaemon error.", e ) ;
            }
            finally {
                try {
                    log.debug( "Daemon sleeping. <<<" ) ;
                    TimeUnit.MINUTES.sleep( 5 ) ;
                }
                catch( Exception ignore ){}
            }
        }
    }

    private Word enrichWord( Word word ) throws Exception {

        WordnikWord wordnikWord = WordnikAdapter.getWordMeaning( word.getWord() ) ;

        word.populate( wordnikWord ) ;

        if( !word.getExamples().isEmpty() && word.getMeanings().size()>1 ) {
            word.setWordnikEnriched( true ) ;
            word = wordRepo.saveAndFlush( word ) ;
            return word ;
        }
        return null ;
    }

    @Override
    public void initialize( SConsole app ) throws Exception {
        super.start() ;
    }

    @Override
    public int getInitializationSequencePreference() {
        return 1000 ;
    }
}
