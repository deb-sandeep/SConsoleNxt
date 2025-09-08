package com.sandy.sconsole.core.api.client.chemspider;

import lombok.Data;

@Data
public class ChemCompound {

    private int chemSpiderId ;
    private String commonName ;
    private String iupacName ;
    private String smiles ;
    private String formula ;
    private float molecularWeight ;
    private float averageMass ;
    private String mol2D ;
    private String mol3D ;
    private String stdInChiKey;
}
