package com.sandy.sconsole.test.core.nvpconfig.helper;

import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@NVPConfigGroup( groupName = "TestComponent" )
public class ObserverComponent {

    @Getter
    @NVPConfig( configName = "configKeyA" )
    private String observedConfig = "" ;
}
