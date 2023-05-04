package com.sandy.sconsole.core.nvpconfig;

import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NVPConfig {
    
    private final NVPConfigDAO nvpDAO ;
    private final NVPConfigDAORepo nvpRepo ;
    
    NVPConfig( NVPConfigDAO nvpDAO, NVPConfigDAORepo nvpRepo ) {
        this.nvpDAO = nvpDAO ;
        this.nvpRepo = nvpRepo ;
    }
    
    public String getConfigName() {
        return nvpDAO.getConfigName() ;
    }
    
    public String getDescription() {
        return nvpDAO.getDescription() ;
    }
    
    public String getGroupName() {
        return nvpDAO.getGroupName() ;
    }

    public Integer getIntValue() {
        return nvpDAO.getIntValue() ;
    }
    
    public Boolean getBooleanValue() {
        return nvpDAO.getBooleanValue() ;
    }
    
    public boolean isTrue() {
        return getBooleanValue() ;
    }
    
    public Date getDateValue() {
        return nvpDAO.getDateValue() ;
    }
    
    public String[] getArrayValue() {
        return nvpDAO.getArrayValue() ;
    }
    
    public List<String> getListValue() {
        return Arrays.asList( getArrayValue() ) ;
    }
    
    public String getValue() {
        return nvpDAO.getValue() ;
    }
    
    public void setValue( Integer i ) {
        nvpDAO.setValue( i ) ;
        save() ;
    }
    
    public void setValue( Boolean b ) {
        nvpDAO.setValue( b ) ;
        save() ;
    }
    
    public void setValue( Date date ) {
        nvpDAO.setValue( date ) ;
        save() ;
    }
    
    public void setValue( String[] values ) {
        nvpDAO.setValue( values ) ;
        save() ;
    }
    
    public void setValue( String value ) {
        nvpDAO.setValue( value ) ;
        save() ;
    }

    void save() {
        this.nvpRepo.save( nvpDAO ) ;
    }
}
