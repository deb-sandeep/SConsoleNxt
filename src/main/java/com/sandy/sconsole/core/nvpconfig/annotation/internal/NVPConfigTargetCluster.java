package com.sandy.sconsole.core.nvpconfig.annotation.internal;

import com.sandy.sconsole.core.nvpconfig.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.NVPConfigChangeListener;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of NVPConfigTarget instances consuming the same NVPConfig
 * (ConfigGroupName + ConfigName).
 */
public class NVPConfigTargetCluster implements NVPConfigChangeListener {

    private final List<NVPConfigTarget> targets = new ArrayList<>() ;

    @Getter
    private final String configGroupName ;

    @Getter
    private final String configName ;

    public NVPConfigTargetCluster( String configGroupName, String configName ) {
        this.configGroupName = configGroupName;
        this.configName = configName ;
    }

    public void add( NVPConfigTarget t ) {
        targets.add( t ) ;
    }

    /**
     * Initializes the target with the current value of the configuration and
     * subsequently registers itself with the NVP manager for any
     * config change updates.
     */
    public void initialize( NVPManager nvpManager ) throws Exception {

        NVPConfig nvpCfg = nvpManager.getConfig( configGroupName, configName ) ;
        if( nvpCfg == null ) {
            // If there are no entries in the database, we create one with
            // the value we find configured in the code in the first target
            // and populate that value in the rest of the targets.
            NVPConfigTarget firstTarget = targets.get( 0 ) ;
            String configVal = firstTarget.getFieldValue() ;
            nvpCfg = nvpManager.getConfig( configGroupName, configName, configVal ) ;
        }

        final NVPConfig finalNvpCfg = nvpCfg ;
        targets.forEach( t -> {
            try {
                t.updateTarget( finalNvpCfg );
            }
            catch( IllegalAccessException e ) {
                throw new RuntimeException( e ) ;
            }
        } ) ;

        nvpManager.addConfigChangeListener( this,
                                            this.getConfigGroupName(),
                                            this.getConfigName() ) ;
    }

    @Override
    public void nvpConfigChanged( NVPConfig nvpCfg ) {
        targets.forEach( t -> {
            try {
                t.updateTarget( nvpCfg );
            }
            catch( IllegalAccessException e ) {
                throw new RuntimeException( e );
            }
        } ) ;
    }

    public String getFQN() {
        return configGroupName + "::" + configName ;
    }
}
