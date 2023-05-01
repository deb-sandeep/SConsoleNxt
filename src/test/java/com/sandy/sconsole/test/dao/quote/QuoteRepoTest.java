package com.sandy.sconsole.test.dao.quote;

import com.sandy.sconsole.dao.quote.QuoteRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.Matchers.* ;
import static org.hamcrest.MatcherAssert.* ;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
public class QuoteRepoTest {

    @Autowired private QuoteRepo quoteRepo ;

    @Test void injectQuoteRepoNotNull() {
        assertThat( quoteRepo, notNullValue() ) ;
    }
}
