package com.sandy.sconsole.core.nvpconfig.annotation.internal;

import com.sandy.sconsole.core.nvpconfig.NVPCfg;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

@Slf4j
public class NVPConfigTarget {

    private final Field field ;
    private final Method notifyMethod ;
    private final Object bean ;
    private final Class<?> fieldClass ;
    private final NVPConfigDAORepo configRepo ;

    @Getter
    private String configGroupName ;

    @Getter
    private String configName ;

    private boolean updateOnChange ;

    public NVPConfigTarget( String defaultCfgGroup,
                            Field field, Method notifyMethod,
                            Object bean, NVPConfigDAORepo configRepo ) {
        this.field = field ;
        this.notifyMethod = notifyMethod ;
        this.bean = bean ;
        this.fieldClass = field.getType() ;
        this.configRepo = configRepo ;

        this.field.setAccessible( true ) ;
        if( this.notifyMethod != null ) {
            this.notifyMethod.setAccessible( true ) ;
        }
        extractConfigName( defaultCfgGroup ) ;
    }

    private void extractConfigName( String defaultCfgGroup ) {

        NVPConfig nvpConfig = field.getAnnotation( NVPConfig.class ) ;
        String groupName = nvpConfig.groupName() ;
        String cfgName = nvpConfig.configName() ;

        this.configName = cfgName.isEmpty() ? field.getName() : cfgName ;
        this.configGroupName = groupName.isEmpty() ? defaultCfgGroup : groupName ;
        this.updateOnChange = nvpConfig.updateOnChange() ;
    }

    public String getFQN() {
        return configGroupName + "::" + configName ;
    }

    public String toString() {
        return configGroupName + "::" + configName + " -> " +
                bean.getClass().getName() + "::" + field.getName() ;
    }

    public void updateTarget( NVPCfg cfg,
                              boolean notify )
            throws IllegalAccessException, InvocationTargetException {

        Object convertedVal = convertCfgValToFieldType( cfg ) ;
        FieldUtils.writeField( this.field, this.bean, convertedVal, true ) ;
        if( notify && this.notifyMethod != null ) {
            if( this.notifyMethod.getParameters().length > 0 ) {
                this.notifyMethod.invoke( this.bean, cfg ) ;
            }
            else {
                this.notifyMethod.invoke( this.bean ) ;
            }
        }
    }

    public void persistState() throws IllegalAccessException {

        String fieldVal = getFieldValue() ;
        NVPConfigDAO dao = configRepo.findByGroupNameAndConfigName( configGroupName, configName ) ;
        if( dao == null ) {
            dao = new NVPConfigDAO( configName, fieldVal ) ;
            dao.setGroupName( configGroupName ) ;
        }
        else {
            dao.setValue( fieldVal ) ;
        }
        configRepo.save( dao ) ;
    }

    public void loadState() throws InvocationTargetException, IllegalAccessException {
        NVPConfigDAO dao = configRepo.findByGroupNameAndConfigName( configGroupName, configName ) ;
        NVPCfg       cfg = new NVPCfg( dao, configRepo ) ;
        updateTarget( cfg, true ) ;
    }

    public String getFieldValue() throws IllegalAccessException {
        return convertFieldValToString() ;
    }

    private Object convertCfgValToFieldType( NVPCfg cfg ) {

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

    private String convertFieldValToString() throws IllegalAccessException {

        Object fieldVal = FieldUtils.readField( this.field, this.bean, true ) ;
        String convertedVal = null ;

        if( fieldVal != null ) {
            convertedVal = fieldVal.toString() ;
            if( fieldClass.equals( Date.class ) ) {
                convertedVal = NVPConfigDAO.SDF.format( (Date)fieldVal ) ;
            }
            else if( fieldClass.equals( String[].class ) ) {
                convertedVal = String.join( ",", (String[])fieldVal ) ;
            }
            else if( fieldClass.equals( List.class ) ) {
                convertedVal = String.join(",", (List<String>)fieldVal ) ;
            }
        }
        return convertedVal ;
    }

}
