package com.sandy.sconsole.test.core.nvpconfig;

import com.sandy.sconsole.SConsole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NVPConfigAnnotationTest {

    @Test void testFreshSingleConfig() {
        assertThat( SConsole.getAppCtx(), is( notNullValue() ) ) ;
    }
}
