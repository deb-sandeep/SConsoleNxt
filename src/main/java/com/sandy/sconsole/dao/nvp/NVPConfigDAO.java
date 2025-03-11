package com.sandy.sconsole.dao.nvp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.util.StringUtil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
@Data
@Entity
@EntityListeners( NVPManager.NVPPersistCallback.class )
@Table( name = "nvp_config" )
public class NVPConfigDAO {
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" ) ;

    @Id
    @GeneratedValue( strategy=GenerationType.IDENTITY )
    private Integer id = null ;
    
    private String groupName = null ;
    private String configName  = null ;
    private String value = null ;
    private String description = null ;
    
    public NVPConfigDAO() {}
    
    public NVPConfigDAO( String name, String value ) {
        this( null, name, value ) ;
    }
    
    public NVPConfigDAO( String group, String name, String value ) {
        this.groupName  = group ;
        this.configName = name ;
        this.value      = value ;
    }
    
    @JsonIgnore
    public Integer getIntValue() {
        return Integer.valueOf( value ) ;
    }
    
    @JsonIgnore
    public Boolean getBooleanValue() {
        return Boolean.valueOf( value ) ;
    }
    
    public void setValue( Integer i ) {
        this.value = i.toString() ;
    }
    
    public void setValue( Boolean b ) {
        this.value = b.toString() ;
    }
    
    @JsonIgnore
    public Date getDateValue() {
        try {
            return SDF.parse( value ) ;
        }
        catch( ParseException e ) {
            log.error( "Error parsing configuration date.", e ) ;
        }
        return null ;
    }
    
    public void setValue( Date date ) {
        this.value = SDF.format( date ) ;
    }
    
    public void setValue( String[] values ) {
        this.value = String.join( ",", values ) ;
    }

    public void setValue( String value ) {
        this.value = value ;
    }

    @JsonIgnore
    public String[] getArrayValue() {
        
        ArrayList<String> valuesList = new ArrayList<>() ;
        for( String val : this.value.split( "," ) ) {
            if( StringUtil.isNotEmptyOrNull( val ) ) {
                valuesList.add( val.trim() ) ;
            }
        }
        return valuesList.toArray( new String[0] ) ;
    }

    public String toString() {
        return "IDGen [\n" +
                "   group = " + this.groupName + "\n" +
                "   name  = " + this.configName + "\n" +
                "   value = " + this.value + "\n" +
                "   desc  = " + this.description + "\n" +
                "]";
    }
}
