package com.sandy.sconsole.test.core.wordnik;

import com.sandy.sconsole.core.wordnic.WordnikAdapter;
import com.sandy.sconsole.core.wordnic.WordnikWord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class WordnikTest {

    @Test void wordnikTest() throws Exception {
        WordnikWord word = WordnikAdapter.getWordMeaning( "remote" ) ;
        assertThat( word, is( notNullValue() ) ) ;
        assertThat( word.getExamples().isEmpty(), is( false ) ) ;
        assertThat( word.getMeanings().isEmpty(), is( false ) ) ;
    }
}
