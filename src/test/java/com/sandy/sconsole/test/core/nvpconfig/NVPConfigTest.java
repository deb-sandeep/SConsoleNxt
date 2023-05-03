package com.sandy.sconsole.test.core.nvpconfig;

import com.sandy.sconsole.core.nvpconfig.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
@SpringBootTest
public class NVPConfigTest {

    private static final String GRP = "testGroup" ;
    private static final String KEY = "testConfig" ;
    private static final String VAL = "testValue" ;

    @Autowired private NVPConfigDAORepo nvpRepo ;
    @Autowired private NVPManager nvpManager ;

    @BeforeEach
    @AfterEach
    void deleteTestConfig() {
        // Delete the test configuration if it exists.
        NVPConfigDAO dao = nvpRepo.findByGroupNameAndConfigName( GRP,KEY ) ;
        if( dao != null ) {
            nvpRepo.delete( dao ) ;
        }
    }

    @Test void test() {

        NVPConfig cfg = nvpManager.getConfig( GRP, KEY, VAL ) ;
        assertThat( cfg, is( notNullValue() ) ) ;

        NVPConfigDAO dao = nvpRepo.findByGroupNameAndConfigName( GRP,KEY ) ;
        assertThat( dao, is( notNullValue() ) ) ;
    }
}
