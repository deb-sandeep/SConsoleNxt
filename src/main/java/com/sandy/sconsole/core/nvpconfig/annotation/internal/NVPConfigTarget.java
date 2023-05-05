package com.sandy.sconsole.core.nvpconfig.annotation.internal;

import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

@Slf4j
public class NVPConfigTarget {

    private final Field field ;
    private final Object bean ;
    private final Class<?> fieldClass ;

    @Getter
    private String configGroupName ;

    @Getter
    private String configName ;

    private boolean updateOnChange ;

    public NVPConfigTarget( String defaultCfgGroup, Field field, Object bean ) {
        this.field = field ;
        this.bean = bean ;
        this.fieldClass = field.getType() ;

        this.field.setAccessible( true ) ;

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

    public String getFQN() {
        return configGroupName + "::" + configName ;
    }

    public String toString() {
        return configGroupName + "::" + configName + " -> " +
                bean.getClass().getName() + "::" + field.getName() ;
    }

    public void updateTarget( com.sandy.sconsole.core.nvpconfig.NVPConfig cfg )
            throws IllegalAccessException {

        log.debug( "Updating NVP target {} with value {}", this, cfg.getValue() );

        Object convertedVal = getConvertedValue( fieldClass, cfg ) ;
        FieldUtils.writeField( this.field, this.bean, convertedVal, true ) ;
    }

    private Object getConvertedValue( Class<?> targetValue,
                                      com.sandy.sconsole.core.nvpconfig.NVPConfig cfg ) {

        Object convertedVal = cfg.getValue() ;
        if( fieldClass.equals( Integer.class ) ||
                fieldClass.equals( int.class ) ) {
            convertedVal = cfg.getIntValue() ;
        }
        else if( fieldClass.equals( Float.class ) ||
                fieldClass.equals( float.class ) ) {
            convertedVal = Float.parseFloat( cfg.getValue() ) ;
        }
        else if( fieldClass.equals( Boolean.class ) ||
                fieldClass.equals( boolean.class ) ) {
            convertedVal = cfg.getBooleanValue() ;
        }
        else if( fieldClass.equals( Date.class ) ) {
            convertedVal = cfg.getDateValue() ;
        }
        else if( fieldClass.equals( String[].class ) ) {
            convertedVal = cfg.getArrayValue() ;
        }
        else if( fieldClass.equals( List.class ) ) {
            convertedVal = cfg.getListValue() ;
        }
        return convertedVal ;
    }
}
