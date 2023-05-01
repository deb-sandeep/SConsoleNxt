package com.sandy.sconsole.test.core.remote;

import com.sandy.sconsole.core.remote.RemoteKey;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class RemoteKeyEnumTest {

    @Test void converstionTest() {
        MatcherAssert.assertThat( RemoteKey.FN_A, Matchers.equalTo( RemoteKey.valueOf( "FN_A" ) ) );
    }
}
