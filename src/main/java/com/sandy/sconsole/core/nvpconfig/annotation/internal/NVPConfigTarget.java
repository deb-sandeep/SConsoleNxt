package com.sandy.sconsole.core.nvpconfig.annotation.internal;

import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class NVPConfigTarget {

    private final Field field ;
    private final Object bean ;

    private String configGroupName ;
    private String configName ;
    private boolean updateOnChange ;

    public NVPConfigTarget( String defaultCfgGroup, Field field, Object bean ) {
        this.field = field ;
        this.bean = bean ;
        extractConfigName( defaultCfgGroup ) ;
        log.debug( "    configGroupName - {}", configGroupName ) ;
        log.debug( "    configName      - {}", configName ) ;
        log.debug( "    updateOnChange  - {}", updateOnChange ) ;
    }

    private void extractConfigName( String defaultCfgGroup ) {

        NVPConfig nvpConfig = field.getAnnotation( NVPConfig.class ) ;
        String groupName = nvpConfig.groupName() ;
        String cfgName = nvpConfig.configName() ;

        this.configName = cfgName.equals( "" ) ? field.getName() : cfgName ;
        this.configGroupName = groupName.equals( "" ) ? defaultCfgGroup : groupName ;
        this.updateOnChange = nvpConfig.updateOnChange() ;
    }

    public void initialize() {

    }

    public String getConfigFQN() {
        return configGroupName + "::" + configName ;
    }

    public String toString() {
        return configGroupName + "::" + configName + " -> " +
                bean.getClass().getName() + "::" + field.getName() ;
    }
}
