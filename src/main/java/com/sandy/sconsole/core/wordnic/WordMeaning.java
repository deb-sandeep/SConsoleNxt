package com.sandy.sconsole.core.wordnic;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class WordMeaning {

    @Getter private final String word ;
    @Getter private List<String> meanings = new ArrayList<>() ;
    @Getter private List<String> examples = new ArrayList<>() ;

    WordMeaning( String word ) {
        this.word = StringUtils.capitalize( word ) ;
    }
}
