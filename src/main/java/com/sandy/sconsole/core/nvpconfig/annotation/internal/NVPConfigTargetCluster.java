package com.sandy.sconsole.core.nvpconfig.annotation.internal;

import com.sandy.sconsole.core.nvpconfig.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.NVPConfigChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of NVPConfigTarget instances consuming the same NVPConfig
 * (ConfigGroupName + ConfigName).
 */
public class NVPConfigTargetCluster implements NVPConfigChangeListener {

    private final List<NVPConfigTarget> targets = new ArrayList<>() ;
    private final String configGroupName ;
    private final String configName ;

    public NVPConfigTargetCluster( String configGroupName, String configName ) {
        this.configGroupName = configGroupName;
        this.configName = configName;
    }

    @Override
    public void nvpConfigChanged( NVPConfig nvp ) {

    }
}
