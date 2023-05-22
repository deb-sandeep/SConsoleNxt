package com.sandy.sconsole.core.wordnic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.core.net.HTTPResourceDownloader;
import com.sandy.sconsole.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class WordnikAdapter {

    private static final String DEFINITION_URL =
            "https://api.wordnik.com/v4/word.json/${word}/definitions?" +
            "limit=50&" +
            "includeRelated=false&" +
            "useCanonical=false&" +
            "includeTags=false&" +
            "api_key=${api_key}" ;

    private static final String EXAMPLES_URL =
            "https://api.wordnik.com/v4/word.json/${word}/examples?" +
            "includeDuplicates=false&" +
            "useCanonical=false&" +
            "limit=50&" +
            "api_key=${api_key}" ;

    public static WordnikWord getWordMeaning( String word )
        throws Exception {
        log.debug( "- Getting word meaning for '{}' from Wordnik.", word ) ;
        return new WordnikAdapter( word ).fetchWordMeaning() ;
    }

    private static final int MAX_MEANING_LENGTH = 150 ;
    private static final int MAX_EXAMPLE_LENGTH = 170 ;

    private final String word ;
    private final HTTPResourceDownloader http ;
    private final Map<String, String> headers = new HashMap<>() ;

    private WordnikAdapter( String word ) {
        this.word = word ;
        this.http = HTTPResourceDownloader.instance() ;
        headers.put( "Accept", "application/json" ) ;
    }

    private WordnikWord fetchWordMeaning() throws Exception {
        WordnikWord wordMeaning = new WordnikWord( word ) ;
        getMeanings( wordMeaning.getMeanings() ) ;
        getExamples( wordMeaning.getExamples() ) ;
        return wordMeaning ;
    }

    private void getMeanings( List<String> meanings )
        throws Exception {

        log.debug( "- Getting meanings." ) ;
        JsonNode jsonRoot = getJsonRootNode( DEFINITION_URL, 3 ) ;

        if( jsonRoot != null ) {
            for( int i=0; i<jsonRoot.size(); i++ ) {

                JsonNode child = jsonRoot.get( i ) ;

                if( child.has( "text" ) ) {
                    String meaning = child.get( "text" ).asText().trim() ;

                    if( StringUtil.isNotEmptyOrNull( meaning ) ) {
                        if( removeMarkups( meaning ).length() <= MAX_MEANING_LENGTH &&
                            !meanings.contains( meaning ) ) {
                            meanings.add( meaning.trim() ) ;
                        }
                    }
                }
            }
        }
        log.debug( "-> Got {} meanings.", meanings.size() ) ;
    }

    private void getExamples( List<String> examples )
        throws Exception {

        log.debug( "- Getting examples." ) ;
        JsonNode jsonRoot = getJsonRootNode( EXAMPLES_URL, 3 ) ;

        if( jsonRoot != null && jsonRoot.has( "examples" ) ) {
            JsonNode examplesNode = jsonRoot.get( "examples" ) ;

            for( int i=0; i<examplesNode.size(); i++ ) {
                JsonNode child = examplesNode.get( i ) ;

                if( child.has( "text" ) ) {
                    String example = child.get( "text" ).asText() ;

                    if( StringUtil.isNotEmptyOrNull( example ) ) {
                        if( removeMarkups( example ).length() <= MAX_EXAMPLE_LENGTH &&
                            !examples.contains( example ) ) {
                            examples.add( example.trim() ) ;
                        }
                    }
                }
            }
        }
        log.debug( "-> Got {} examples.", examples.size() ) ;
    }

    private JsonNode getJsonRootNode( String url, int remainingTries )
            throws Exception {

        JsonNode jsonRoot ;
        try {
            String       fmtUrl    = getFormattedURL( url ) ;
            String       jsonStr   = this.http.getResource( fmtUrl, headers ) ;
            ObjectMapper objMapper = new ObjectMapper() ;

            jsonRoot  = objMapper.readTree( jsonStr ) ;
        }
        catch( SocketTimeoutException ste ) {

            log.debug( "->  Socket timeout." ) ;
            if( remainingTries >= 1 ) {
                log.debug( "->  Timeout detected. Retrying" ) ;
                try {
                    Thread.sleep( 2000 ) ;
                }
                catch( InterruptedException ignored ) {}
                return getJsonRootNode( url, remainingTries-1 ) ;
            }
            else {
                log.debug( "->> Timeout detected. Max retries quenched." ) ;
                throw new SocketTimeoutException( "Socket timeout after retries." ) ;
            }
        }
        return jsonRoot ;
    }

    private String getFormattedURL( String url ) {
        return url.replace( "${word}", this.word )
                  .replace( "${api_key}", System.getenv( "WORDNIK_API_KEY" ) ) ;
    }

    private String removeMarkups( String input ) {
        return input.replaceAll( "<.*?>", "" ) ;
    }
}
