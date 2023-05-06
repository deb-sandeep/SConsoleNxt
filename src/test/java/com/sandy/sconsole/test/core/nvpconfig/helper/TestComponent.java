package com.sandy.sconsole.test.core.nvpconfig.helper;

import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigChangeListener;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NVPConfigGroup
public class TestComponent {

    @Getter @Setter @NVPConfig
    private String configKeyA = "initial_value" ;

    @Getter @NVPConfig
    private boolean booleanFlag = true ;

    @Getter @Setter
    private int numConfigUpdatesCalled = 0 ;

    @NVPConfigChangeListener
    private void configUpdated( com.sandy.sconsole.core.nvpconfig.NVPConfig cfg  ) {
        numConfigUpdatesCalled++ ;
    }
}
