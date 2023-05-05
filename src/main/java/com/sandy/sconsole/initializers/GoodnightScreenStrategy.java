package com.sandy.sconsole.initializers;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.SystemInitializer;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import org.springframework.stereotype.Component;

@Component
@NVPConfigGroup( groupName="GoodnightScreenStrategy" )
public class GoodnightScreenStrategy implements SystemInitializer {

    @NVPConfig
    private boolean enableStrategy = true ;

    @Override
    public void initialize( SConsole app ) throws Exception {
    }
}
