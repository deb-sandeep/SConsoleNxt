package com.sandy.sconsole.test.core.nvpconfig;

import com.sandy.sconsole.core.nvpconfig.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.NVPConfigChangeListener;
import com.sandy.sconsole.core.nvpconfig.NVPConfigGroup;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
@SpringBootTest
public class NVPConfigTest {

    private static final String GRP  = "testGroup" ;
    private static final String KEY1 = "testConfig1" ;
    private static final String VAL1 = "testValue1" ;
    private static final String KEY2 = "testConfig2" ;
    private static final String VAL2 = "testValue2" ;

    @Autowired private NVPConfigDAORepo nvpRepo ;
    @Autowired private NVPManager nvpManager ;

    @BeforeEach
    @AfterEach
    void deleteTestConfig() {

        nvpManager.removeAllConfigChangeListeners() ;

        // Delete the test configuration if it exists.
        List<NVPConfigDAO> daos = nvpRepo.findByGroupName( GRP ) ;
        if( daos != null ) {
            nvpRepo.deleteAll( daos );
        }
    }

    @Test void testFreshSingleConfig() {

        NVPConfig cfg = nvpManager.getConfig( GRP, KEY1, VAL1 ) ;
        assertThat( cfg, is( notNullValue() ) ) ;

        NVPConfigDAO dao = nvpRepo.findByGroupNameAndConfigName( GRP, KEY1 ) ;
        assertThat( dao, is( notNullValue() ) ) ;
    }

    @Test void testGroupConfig() {

        NVPConfigGroup cfgGrp = nvpManager.getConfigGroup( GRP ) ;
        assertThat( cfgGrp, is( notNullValue() ) ) ;
        assertThat( cfgGrp.getGroupName(), equalTo( GRP ) ) ;
        assertThat( cfgGrp.getStringValue( KEY1, "X" ), equalTo( "X" ) ) ;
        assertThat( cfgGrp.getStringValue( KEY2, "X" ), equalTo( "X" ) ) ;

        cfgGrp.setValue( KEY1, VAL1 ) ;
        cfgGrp.setValue( KEY2, VAL2 ) ;

        List<NVPConfigDAO> daos = nvpRepo.findByGroupName( GRP ) ;
        assertThat( daos, is( notNullValue() ) ) ;
        assertThat( daos.size(), is( equalTo( 2 ) ) ) ;
        assertThat( daos.get( 0 ).getValue(), anyOf( equalTo( VAL1 ), equalTo( VAL2 ) ) ) ;
        assertThat( daos.get( 1 ).getValue(), anyOf( equalTo( VAL1 ), equalTo( VAL2 ) ) ) ;
    }

    @Test void configChange() {

        final List<String> changedValues = new ArrayList<>() ;
        NVPConfigChangeListener listener = new NVPConfigChangeListener() {
            @Override
            public void propertyChanged( NVPConfig nvp ) {
                changedValues.add( nvp.getValue() ) ;
            }
        } ;

        nvpManager.addConfigChangeListener( listener, GRP ) ;
        nvpManager.addConfigChangeListener( listener, GRP, KEY1  );

        nvpManager.getConfig( GRP, KEY1, "X" ).setValue( "NEW" ) ;

        assertThat( changedValues.size(), is( equalTo( 1 ) ) ) ;
        assertThat( changedValues.get( 0 ), is( equalTo( "NEW" ) ) ) ;
    }
}
