package com.sandy.sconsole.test.core.nvpconfig.annotation;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NVPConfigAnnotationTest {

    private NVPConfigAnnotationProcessor processor = new NVPConfigAnnotationProcessor() ;

    @Test void testFreshSingleConfig() {
        assertThat( SConsole.getAppCtx(), is( notNullValue() ) ) ;
        processor.processNVPConfigAnnotations( SConsole.getAppCtx() ) ;
    }
}
