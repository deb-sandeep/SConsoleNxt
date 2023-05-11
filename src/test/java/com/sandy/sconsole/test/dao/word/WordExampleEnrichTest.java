package com.sandy.sconsole.test.dao.word;

import com.sandy.sconsole.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class WordExampleEnrichTest {

    @Test
    void testEnrich1() {
        String toEnrich = "Am serah buri buri nimpah, serah pe." ;
        String enriched = "Am <font color=white>serah</font> buri buri nimpah, <font color=white>serah</font> pe." ;
        String enrichedStr = StringUtil.enrichExample( toEnrich, "serah" ) ;

        assertThat( enrichedStr, is( equalTo( enriched ) ) ) ;
     }

    @Test
    void testEnrich2() {
        String toEnrich = "Am serah buri buri nimpah, serah pe." ;
        String enriched = "<font color=white>Am</font> serah buri buri nimpah, serah pe." ;
        String enrichedStr = StringUtil.enrichExample( toEnrich, "Am" ) ;

        assertThat( enrichedStr, is( equalTo( enriched ) ) ) ;
     }

    @Test
    void testEnrich3() {
        String toEnrich = "Am serah buri buri nimpah, serah pe" ;
        String enriched = "Am serah buri buri nimpah, serah <font color=white>pe</font>" ;
        String enrichedStr = StringUtil.enrichExample( toEnrich, "pe" ) ;

        assertThat( enrichedStr, is( equalTo( enriched ) ) ) ;
     }
}
