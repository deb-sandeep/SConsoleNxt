package com.sandy.sconsole.core.nvpconfig;

import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.GenericValidator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class NVPCfgGroup {

    private static final String SIMPLE_SDF_FMT = "dd-MM-yyyy" ;
    private static final SimpleDateFormat SIMPLE_SDF = new SimpleDateFormat( SIMPLE_SDF_FMT ) ;

    private final Map<String, NVPCfg> childCfgs = new HashMap<>() ;
    private final NVPConfigDAORepo    nvpRepo ;
    
    @Getter private final String groupName ;

    public NVPCfgGroup( String groupName, NVPConfigDAORepo nvpRepo ) {
        this.groupName = groupName ;
        this.nvpRepo = nvpRepo ;
    }
    
    void addNVPConfig( NVPCfg cfg ) {
        this.childCfgs.put( cfg.getConfigName(), cfg ) ;
    }
    
    private NVPCfg getNVPConfig( String cfgKey, String defaultValue ) {
        NVPCfg cfg = this.childCfgs.get( cfgKey ) ;
        if( cfg == null ) {
            NVPConfigDAO nvp = new NVPConfigDAO( cfgKey, defaultValue ) ;
            nvp.setGroupName( groupName ) ;
            nvp = nvpRepo.save( nvp ) ;
            cfg = new NVPCfg( nvp, nvpRepo ) ;
            this.childCfgs.put( cfgKey, cfg ) ;
        }
        return cfg ;
    }
    
    public String getStringValue( String cfgKey, String defaultValue ) {
        NVPCfg cfg = getNVPConfig( cfgKey, defaultValue ) ;
        return cfg.getValue() ;
    }
    
    public Integer getIntValue( String cfgKey, int defaultValue ) {
        NVPCfg cfg = getNVPConfig( cfgKey, Integer.toString( defaultValue ) ) ;
        return cfg.getIntValue() ;
    }
    
    public Boolean getBoolValue( String cfgKey, boolean defaultValue ) {
        NVPCfg cfg = getNVPConfig( cfgKey, Boolean.toString( defaultValue ) ) ;
        return cfg.getBooleanValue() ;
    }
    
    public Date getDateValue( String cfgKey, Date defaultValue ) {
        NVPCfg cfg = getNVPConfig( cfgKey, NVPConfigDAO.SDF.format( defaultValue ) ) ;
        return cfg.getDateValue() ;
    }
    
    public Date getDateValue( String cfgKey, String defaultValue ) {
        Date defaultDate = null ;
        try {
            if( GenericValidator.isDate( defaultValue, SIMPLE_SDF_FMT, true ) ) {
                defaultDate = SIMPLE_SDF.parse( defaultValue ) ;
            }
            else {
                defaultDate = NVPConfigDAO.SDF.parse( defaultValue ) ;
            }
        }
        catch( ParseException e ) {
            log.error( "Unparseable date " + defaultValue, e ) ;
        }
        return this.getDateValue( cfgKey, defaultDate ) ;
    }

    public String[] getArrayValue( String cfgKey, String defaultValue ) {
        NVPCfg cfg = getNVPConfig( cfgKey, defaultValue ) ;
        return cfg.getArrayValue() ;
    }

    public List<String> getListValue( String cfgKey, String defaultValue ) {
        NVPCfg cfg = getNVPConfig( cfgKey, defaultValue ) ;
        return cfg.getListValue() ;
    }

    public void setValue( String cfgKey, int i ) {
        NVPCfg cfg = getNVPConfig( cfgKey, Integer.toString( i ) ) ;
        cfg.setValue( i ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, boolean b ) {
        NVPCfg cfg = getNVPConfig( cfgKey, Boolean.toString( b ) ) ;
        cfg.setValue( b ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, Date date ) {
        if( date == null ) {
            throw new IllegalArgumentException( "Cfg date value is null. " + 
                                                "Key = " + cfgKey ) ;
        }
        
        NVPCfg cfg = getNVPConfig( cfgKey, NVPConfigDAO.SDF.format( date ) ) ;
        cfg.setValue( date ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, String[] values ) {
        if( values == null ) {
            throw new IllegalArgumentException( "Cfg array value is null. " + 
                                                "Key = " + cfgKey ) ;
        }
        NVPCfg cfg = getNVPConfig( cfgKey, String.join( ",", values ) ) ;
        cfg.setValue( values ) ;
        cfg.save() ;
    }

    public void setValue( String cfgKey, String cfgVal ) {
        if( cfgVal == null ) {
            throw new IllegalArgumentException( "Cfg value is null. " +
                                                "Key = " + cfgKey ) ;
        }
        NVPCfg cfg = getNVPConfig( cfgKey, cfgVal ) ;
        cfg.setValue( cfgVal ) ;
        cfg.save() ;
    }
}
