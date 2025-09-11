package com.sandy.sconsole.core.api.client.chemspider;

import com.sandy.sconsole.dao.chem.ChemCompoundDBO;
import lombok.Data;

@Data
public class ChemCompound {

    private int id ;
    private int chemSpiderId ;
    private String commonName ;
    private String iupacName ;
    private String smiles ;
    private String formula ;
    private float molecularWeight ;
    private float averageMass ;
    private String mol2D ;
    private String mol3D ;
    private String compactFormula ;
    private boolean cardDownloaded = false;
    
    public ChemCompound() {}
    
    public ChemCompound( ChemCompoundDBO dbo ) {
        this.id = dbo.getId() ;
        this.chemSpiderId = dbo.getChemSpiderId() ;
        this.commonName = dbo.getCommonName() ;
        this.iupacName = dbo.getIupacName() ;
        this.smiles = dbo.getSmiles() ;
        this.formula = dbo.getFormula() ;
        this.molecularWeight = dbo.getMolecularWeight() ;
        this.averageMass = dbo.getAverageMass() ;
        this.mol2D = dbo.getMol2D() ;
        this.mol3D = dbo.getMol3D() ;
        this.compactFormula = dbo.getCompactFormula() ;
        this.cardDownloaded = dbo.getCardDownloaded() ;
    }
}
