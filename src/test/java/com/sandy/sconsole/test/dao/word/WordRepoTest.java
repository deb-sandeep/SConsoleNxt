package com.sandy.sconsole.test.dao.word;

import com.sandy.sconsole.core.wordnic.WordnikAdapter;
import com.sandy.sconsole.core.wordnic.WordnikWord;
import com.sandy.sconsole.dao.word.Word;
import com.sandy.sconsole.dao.word.WordRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
public class WordRepoTest {

    @Autowired private WordRepo wordRepo ;

    @Test void injectWordRepoNotNull() {
        assertThat( wordRepo, notNullValue() ) ;
    }

    @Test void testFindByword() {
        assertThat( wordRepo.findByWord( "abase" ), is(notNullValue()) ) ;
    }

    @Test void saveWord() throws Exception {
        Word word = wordRepo.findByWord( "amorally" ) ;
        WordnikWord wordnikWord = WordnikAdapter.getWordMeaning( "amorally" ) ;
        assertThat( wordnikWord, is(notNullValue()) ) ;

        word.populate( wordnikWord ) ;
        word = wordRepo.saveAndFlush( word ) ;
        assertThat( word.getExamples().size(), is(greaterThan( 0 ) ) ) ;
        assertThat( word.getMeanings().size(), is(greaterThan( 0 ) ) ) ;
    }
}
