package com.sandy.sconsole.daemon.wordnik;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.daemon.DaemonBase;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
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

    @Autowired
    private WordRepo wordRepo ;

    @Autowired
    private NVPManager nvpManager ;

    @NVPConfig
    private int runDelaySec = 180 ;

    @NVPConfig
    private boolean enabled = true ;

    public WordnikEnricherDaemon(){
        super( "WordnikEnricherDaemon" ) ;
    }

    public void run() {
        try { Thread.sleep( 5000 ) ; } catch( InterruptedException ignore ) {}

        while( true ) {
            try {
                if( enabled ) {
                    log.debug( "Wordnik daemon commencing run." );
                    Word wordDAO = wordRepo.findWordForEnrichment() ;
                    String word = null ;

                    if( wordDAO == null ) {
                        log.info( "  No more words left to be enriched." ) ;
                    }
                    else {
                        word = wordDAO.getWord() ;
                        log.debug( "  Enriching word {} >", word ) ;
                        Word enrichedWord = null ;
                        try {
                            enrichedWord = enrichWord( wordDAO ) ;
                        }
                        catch( Exception e ) {
                            log.error( "  Wordnik adapter error.", e ) ;
                        }

                        if( enrichedWord != null ) {
                            log.debug( "  Enriched with {} meanings and {} examples.",
                                    wordDAO.getMeanings().size(),
                                    wordDAO.getExamples().size() );
                        }
                        else {
                            log.debug( "  Skipped enriching word." ) ;
                            wordDAO = wordRepo.findByWord( word ) ;
                            wordDAO.setWordnikEnriched( false ) ;
                            wordDAO.setNumWordnikTries( wordDAO.getNumWordnikTries() + 1 ) ;
                            wordRepo.save( wordDAO ) ;
                        }
                    }
                }
            }
            finally {
                try {
                    nvpManager.loadNVPConfigState( this ) ;
                    log.debug( "Daemon run completed. Sleeping for {} seconds.", runDelaySec ) ;
                    TimeUnit.SECONDS.sleep( runDelaySec ) ;
                }
                catch( Exception e ){
                    log.error( "Unanticipated exception in Wordnik daemon. Terminating.", e ) ;
                    throw new RuntimeException( e ) ;
                }
            }
        }
    }

    private Word enrichWord( Word word ) throws Exception {

        WordnikWord wordnikWord = WordnikAdapter.getWordMeaning( word.getWord() ) ;

        word.populate( wordnikWord ) ;

        if( !word.getExamples().isEmpty() && word.getMeanings().size()>1 ) {
            word.setWordnikEnriched( true ) ;
            word.setNumWordnikTries( word.getNumWordnikTries() + 1 ) ;
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
