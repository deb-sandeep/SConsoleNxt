package com.sandy.sconsole.test.core.nvpconfig;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.nvpconfig.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import com.sandy.sconsole.test.core.nvpconfig.helper.TestComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NVPConfigAnnotationTest {

    @Autowired
    private NVPManager nvpManager ;

    @Autowired
    private NVPConfigDAORepo nvpRepo ;

    @Autowired
    private TestComponent testComponent ;

    private NVPConfigAnnotationProcessor processor = new NVPConfigAnnotationProcessor() ;

    @BeforeEach
    void beforeEach() {
        List<NVPConfigDAO> daos = nvpRepo.findByGroupName(
                                        TestComponent.class.getSimpleName() ) ;
        nvpRepo.deleteAll( daos ) ;

        daos = nvpRepo.findByGroupName( "TestCfgGroup" ) ;
        nvpRepo.deleteAll( daos ) ;
    }

    /**
     * A persisted configuration is created when the components with wired
     * NVPConfig fields are loaded by the application context.
     */
    @Test void testFreshSingleConfig() {

        processor.processNVPConfigAnnotations( SConsole.getAppCtx(),
                                               TestComponent.class.getPackageName() ) ;

        NVPConfig cfg = nvpManager.getConfig( "TestComponent", "configKeyA" ) ;
        assertThat( cfg, is( notNullValue() ) ) ;
    }

    /**
     * Change a NVPConfig through the NVPManager and the configurations in wired
     * components are auto updated.
     */
    @Test void testConfigUpdate() {

        processor.processNVPConfigAnnotations( SConsole.getAppCtx(),
                                               TestComponent.class.getPackageName() ) ;

        NVPConfig cfg = nvpManager.getConfig( "TestComponent", "configKeyA" ) ;
        cfg.setValue( "changed_value" ) ;

        assertThat( testComponent.getConfigKeyA(), is( equalTo( "changed_value" ) ) ) ;
    }
}
