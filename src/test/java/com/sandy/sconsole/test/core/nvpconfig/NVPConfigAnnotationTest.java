package com.sandy.sconsole.test.core.nvpconfig;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.nvpconfig.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import com.sandy.sconsole.test.core.nvpconfig.helper.ObserverComponent;
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

    @Autowired
    private ObserverComponent observerComponent ;

    private NVPConfigAnnotationProcessor processor = null ;

    @BeforeEach
    void beforeEach() {
        List<NVPConfigDAO> daos = nvpRepo.findByGroupName(
                                        TestComponent.class.getSimpleName() ) ;
        nvpRepo.deleteAll( daos ) ;

        daos = nvpRepo.findByGroupName( "TestCfgGroup" ) ;
        nvpRepo.deleteAll( daos ) ;

        processor = new NVPConfigAnnotationProcessor( SConsole.getAppCtx() ) ;
    }

    /**
     * A persisted configuration is created when the components with wired
     * NVPConfig fields are loaded by the application context.
     */
    @Test void testFreshSingleConfig() {

        processor.processNVPConfigAnnotations( TestComponent.class.getPackageName() ) ;

        NVPConfig cfg = nvpManager.getConfig( "TestComponent", "configKeyA" ) ;
        assertThat( cfg, is( notNullValue() ) ) ;
    }

    /**
     * Change a NVPConfig through the NVPManager and the configurations in wired
     * components are auto updated.
     */
    @Test void testConfigUpdate() {

        NVPConfig cfg ;

        processor.processNVPConfigAnnotations( TestComponent.class.getPackageName() ) ;

        cfg = nvpManager.getConfig( "TestComponent", "configKeyA" ) ;
        cfg.setValue( "changed_value" ) ;
        assertThat( testComponent.getConfigKeyA(), is( equalTo( "changed_value" ) ) ) ;

        cfg = nvpManager.getConfig( "TestComponent", "booleanFlag" ) ;
        cfg.setValue( true ) ;
        assertThat( cfg.getBooleanValue(), is( true ) ) ;
    }

    /**
     * NVPManager can persist changes to fields and notify any wired components
     */
    @Test void persistState() throws IllegalAccessException {

        processor.processNVPConfigAnnotations( TestComponent.class.getPackageName() ) ;

        // Update the NVPConfig field of an object programmatically
        testComponent.setConfigKeyA( "some_changed_value" ) ;
        nvpManager.persistNVPConfigState( testComponent ) ;

        assertThat( observerComponent.getObservedConfig(),
                    is( equalTo( "some_changed_value" ) ) ) ;
    }
}
