package com.sandy.sconsole.utility;

import com.sandy.sconsole.core.util.AbstractUtility;
import com.sandy.sconsole.dao.word.Word;
import com.sandy.sconsole.dao.word.WordRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Slf4j
@SpringBootApplication
@ComponentScan( basePackages = { "com.sandy.sconsole.*" } )
@EntityScan( "com.sandy.sconsole.*" )
@EnableJpaRepositories( "com.sandy.sconsole.dao.*" )
public class WOTDUtility extends AbstractUtility {

    public static void main( String[] args ) {
        try {
            log.debug( "Starting Utility - {}", MethodHandles.lookup().lookupClass() ) ;
            log.debug( "DB_HOST = {}", System.getenv( "DB_HOST" ) ) ;

            SpringApplication.run( WOTDUtility.class, args ) ;
            WOTDUtility utility = AbstractUtility.appCtx.getBean( WOTDUtility.class ) ;
            utility.execute() ;
        }
        catch( Exception e ) { log.error( "Could not launch utility.", e ) ; }
        finally {
            AbstractUtility.closeAppContext() ;
        }
    }

    @Autowired WordRepo wordRepo ;

    public void execute() throws Exception {
        log.debug( "Executing utility." ) ;
        List<String> lines = super.getUtilityResourceFileContents( "wotd", "vocab.txt" ) ;

        lines.forEach( l -> {
            String[] parts = l.split( "@" ) ;

            String word = parts[0].trim().toLowerCase() ;
            String freq = parts[1].trim() ;
            String mean = parts[2].trim() ;
            String expl = null ;

            if( parts.length > 3 ) {
                expl = parts[3].trim() ;
            }

            Word wordDO = wordRepo.findByWord( word ) ;
            if( wordDO == null ) {
                wordDO = new Word() ;
                wordDO.setWord( parts[0].trim() ) ;
            }

            wordDO.setFrequency( getFrequency( freq ) ) ;
            wordDO.setMeaning( mean ) ;
            wordDO.setExample( expl ) ;

            log.debug( "Saving word {}", word ) ;
            wordRepo.save( wordDO ) ;
        } ) ;
    }

    private float getFrequency( String input ) {
        try {
            return Float.parseFloat( input ) ;
        }
        catch( Exception e ) {
            return 0 ;
        }
    }
}

