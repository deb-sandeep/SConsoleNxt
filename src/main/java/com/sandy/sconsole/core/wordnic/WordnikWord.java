package com.sandy.sconsole.core.wordnic;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class WordnikWord {

    @Getter private final String word ;
    @Getter private final List<String> meanings = new ArrayList<>() ;
    @Getter private final List<String> examples = new ArrayList<>() ;

    WordnikWord( String word ) {
        this.word = StringUtils.capitalize( word ) ;
    }
}
