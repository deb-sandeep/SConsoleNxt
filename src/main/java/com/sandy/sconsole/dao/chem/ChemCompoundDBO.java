package com.sandy.sconsole.dao.chem;

import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "chem_compound" )
public class ChemCompoundDBO {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Column( name = "chem_spider_id" )
    private Integer chemSpiderId;
    
    @Column( name = "common_name", length = 128 )
    private String commonName;
    
    @Column( name = "iupac_name", length = 256 )
    private String iupacName;
    
    @Column( name = "smiles", length = 256 )
    private String smiles;
    
    @Column( name = "formula", length = 256 )
    private String formula;
    
    @Column( name = "molecular_weight" )
    private Float molecularWeight;
    
    @Column( name = "average_mass" )
    private Float averageMass;
    
    @Column( name = "mol_2d", length = 4096 )
    private String mol2D;
    
    @Column( name = "mol3d", length = 4096 )
    private String mol3D;
    
    @Column( name = "compact_formula", length = 256 )
    private String compactFormula;
    
    public ChemCompoundDBO() {}
    
    public ChemCompoundDBO( ChemCompound cc ) {
        mergeValues( cc ) ;
    }
    
    public void mergeValues( ChemCompound cc ) {
        this.chemSpiderId = cc.getChemSpiderId();
        this.commonName = cc.getCommonName();
        this.iupacName = cc.getIupacName();
        this.smiles = cc.getSmiles();
        this.formula = cc.getFormula();
        this.compactFormula = cc.getCompactFormula();
        this.molecularWeight = cc.getMolecularWeight();
        this.averageMass = cc.getAverageMass();
        this.mol2D = cc.getMol2D();
        this.mol3D = cc.getMol3D();
    }
}
