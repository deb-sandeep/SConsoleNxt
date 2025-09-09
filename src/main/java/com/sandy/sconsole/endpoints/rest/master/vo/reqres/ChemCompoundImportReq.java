package com.sandy.sconsole.endpoints.rest.master.vo.reqres;

import lombok.Data;

@Data
public class ChemCompoundImportReq {
    
    public static final String IMPORT_TYPE_NAME = "name" ;
    public static final String IMPORT_TYPE_FORMULA = "formula" ;
    public static final String IMPORT_TYPE_SMILES = "smiles" ;
    
    private String importType ;
    private String name ;
    private String formula ;
    private String smiles ;
    private boolean forceImport ;
    
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer( "ChemCompoundImportReq{" );
        sb.append( "importType='" ).append( importType ).append( '\'' );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( ", formula='" ).append( formula ).append( '\'' );
        sb.append( ", smiles='" ).append( smiles ).append( '\'' );
        sb.append( ", forceImport=" ).append( forceImport );
        sb.append( '}' );
        return sb.toString();
    }
}
