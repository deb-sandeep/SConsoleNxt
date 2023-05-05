package com.sandy.sconsole.initializers;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import org.springframework.stereotype.Component;

@Component
@NVPConfigGroup( groupName="GoodnightScreenStrategy" )
public class GoodnightScreenStrategy implements ComponentInitializer {

    @NVPConfig
    private boolean enableStrategy = true ;

    @Override
    public void initialize( SConsole app ) throws Exception {
    }
}
