package com.sandy.sconsole.test.core.nvpconfig.helper;

import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@NVPConfigGroup
public class TestComponent {

    @Getter
    @NVPConfig
    private String configKeyA = "initial_value" ;
}
