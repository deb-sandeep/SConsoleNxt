package com.sandy.sconsole.test.core.wordnik;

import com.sandy.sconsole.core.wordnic.WordnicAdapter;
import org.junit.jupiter.api.Test;

public class WordnikTest {

    @Test void wordnikTest() throws Exception {
        WordnicAdapter.getWordMeaning( "remote" ) ;
    }
}
