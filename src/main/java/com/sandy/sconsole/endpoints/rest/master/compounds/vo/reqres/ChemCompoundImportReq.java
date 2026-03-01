package com.sandy.sconsole.endpoints.rest.master.compounds.vo.reqres;

import lombok.Data;

@Data
public class ChemCompoundImportReq {
    
    public static final String IMPORT_TYPE_NAME = "name" ;
    public static final String IMPORT_TYPE_FORMULA = "formula" ;
    public static final String IMPORT_TYPE_SMILES = "smiles" ;
    
    private String importType ;
    private String filterText ;
    private boolean forceImport ;
    
    @Override
    public String toString() {
        return "ChemCompoundImportReq{" +
                "importType='" + importType + '\'' +
                ", filterText='" + filterText + '\'' +
                ", forceImport=" + forceImport +
                '}';
    }
}
