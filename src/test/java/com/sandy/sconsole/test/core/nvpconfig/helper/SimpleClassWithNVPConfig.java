package com.sandy.sconsole.test.core.nvpconfig.helper;

import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import lombok.Getter;

public class SimpleClassWithNVPConfig {

    @Getter
    @NVPConfig( groupName = "TestComponent", configName = "configKeyA" )
    private String cfgVal = "" ;
}
