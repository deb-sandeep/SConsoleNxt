package com.sandy.sconsole.core.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public final class StringUtil {

    public static boolean isEmptyOrNull( final String str ) {
        return ( str == null || str.trim().isEmpty() ) ;
    }

    public static boolean isNotEmptyOrNull( final String str ) {
        return !isEmptyOrNull( str ) ;
    }
    
    public static String getHash( String input ) {
        return new String( Hex.encodeHex( DigestUtils.md5( input ) ) ) ;
    }
    
    public static String getElapsedTimeLabel( long seconds ) {
        int secs    = (int)(seconds) % 60 ;
        int minutes = (int)((seconds / 60) % 60) ;
        int hours   = (int)(seconds / (60*60)) ;
        
        return String.format("%02d:%02d:%02d", hours, minutes, secs ) ;
    }

    public static String enrichExample( String strToEnrich, String word ) {

        // This means that the string is already enriched. Skip doing it again.
        if( strToEnrich.contains( "<font color=white>" ) ) {
            return strToEnrich ;
        }

        String enrichedString = strToEnrich ;

        enrichedString = enrichedString.replace( ( char )0x2003,' ' ) ;
        enrichedString = StringUtils.capitalize( enrichedString.trim() ) ;

        String orginalString = enrichedString ;

        int indexOfWord = enrichedString.toLowerCase()
                                        .indexOf( word.toLowerCase() ) ;

        while( indexOfWord != -1 ) {
            String enrichedWord =
                    "<font color=white>" +
                    enrichedString.substring( indexOfWord, indexOfWord + word.length() ) +
                    "</font>" ;

            enrichedString = enrichedString.substring( 0, indexOfWord ) +
                             enrichedWord +
                             enrichedString.substring( indexOfWord + word.length() ) ;

            indexOfWord = enrichedString.toLowerCase()
                                 .indexOf( word.toLowerCase(),
                                          indexOfWord + enrichedWord.length() ) ;
        }

        return enrichedString ;
    }
}
