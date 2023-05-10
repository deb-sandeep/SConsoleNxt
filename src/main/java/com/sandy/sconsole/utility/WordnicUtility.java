package com.sandy.sconsole.utility;

import com.sandy.sconsole.core.net.HTTPResourceDownloader;
import com.sandy.sconsole.core.util.AbstractUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication
@ComponentScan( basePackages = { "com.sandy.sconsole.*" } )
public class WordnicUtility extends AbstractUtility {

    private static final String URL =
            "https://api.wordnik.com/v4/word.json/${word}/definitions?limit=10&includeRelated=false&useCanonical=false&includeTags=false&api_key=${api_key}" ;

    public static void main( String[] args ) {
        try {
            log.debug( "Starting Utility - {}", MethodHandles.lookup().lookupClass() ) ;
            log.debug( "DB_HOST = {}", System.getenv( "DB_HOST" ) ) ;
            log.debug( "API Key = {}", System.getenv( "WORDNIK_API_KEY" ) ) ;

//            SpringApplication.run( WordnicUtility.class, args ) ;
//            WordnicUtility utility = AbstractUtility.appCtx.getBean( WordnicUtility.class ) ;
//            utility.execute() ;
            new WordnicUtility().execute() ;
        }
        catch( Exception e ) { log.error( "Could not launch utility.", e ) ; }
        finally {
            AbstractUtility.closeAppContext() ;
        }
    }

    public void execute() throws Exception {
        log.debug( "Executing utility." ) ;
        HTTPResourceDownloader http = HTTPResourceDownloader.instance() ;
        Map<String, String> headers = new HashMap<>() ;
        String apiKey = System.getenv( "WORDNIK_API_KEY" ) ;

        headers.put( "Accept", "application/json" ) ;
        String url = URL.replace( "${word}", "siren" ) ;
        url = url.replace( "${api_key}", apiKey ) ;

        String response = http.getResource( url, headers ) ;
        log.debug( response ) ;
    }
}


