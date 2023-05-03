package com.sandy.sconsole.core.nvpconfig;

import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import lombok.Data;

@Data
public class NVP {

    private Integer id          = null ;
    private String  groupName   = null ;
    private String  configName  = null ;
    private String  value       = null ;
    private String  description = null ;
    private boolean boolFlag    = false ;
    
    public NVP() {}
    
    public NVP( NVPConfigDAO master ) {
        this.id          = master.getId() ;
        this.groupName   = master.getGroupName() ;
        this.configName  = master.getConfigName() ;
        this.value       = master.getValue() ;
        this.description = master.getDescription() ;
        this.boolFlag    = this.value != null && 
                           ( this.value.equalsIgnoreCase( "true" ) || 
                             this.value.equalsIgnoreCase( "false" ) ) ;
    }

    public String toString() {
        return "NVPVO [\n" +
                "   group = " + this.groupName + "\n" +
                "   name  = " + this.configName + "\n" +
                "   value = " + this.value + "\n" +
                "   desc  = " + this.description + "\n" +
                "]";
    }
}
