package com.sandy.sconsole.test.dao.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

@Slf4j
public class WordExampleEnrichTest {

    private String enrichExample( String example, String word ) {

        String orginalString = example ;

        example = example.replace( ( char )0x2003,' ' ) ;
        example = StringUtils.capitalize( example.trim() ) ;

        int indexOfWord = example.toLowerCase().indexOf( word.toLowerCase() ) ;
        StringBuilder sb = new StringBuilder() ;
        sb.append( orginalString, 0, indexOfWord ) ;
        sb.append( "<font color='LightGray'>" ) ;
        sb.append( orginalString, indexOfWord, indexOfWord+word.length() ) ;
        sb.append( "</font>" ) ;
        sb.append( orginalString, indexOfWord+word.length(), example.length()) ;

        return sb.toString() ;
    }

    @Test
    void testEnrich() {
        String toEnrich = "I am a good boy." ;
        String enrichedStr = enrichExample( toEnrich, "I" ) ;
        log.debug( enrichedStr ) ;
    }
}
