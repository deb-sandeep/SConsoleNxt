package com.sandy.sconsole.poc;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;

public class IndigoTest {
    public static void main( String[] args ) {
        Indigo indigo = new Indigo() ;
        
        IndigoObject mol = indigo.loadMoleculeFromFile( "/Users/sandeep/temp/" + ChemSpiderClientTest.FORMULA + ".mol" ) ;
        mol.layout() ;
        
        IndigoRenderer renderer = new IndigoRenderer( indigo ) ;
        indigo.setOption( "render-output-format", "png" ) ;
        indigo.setOption( "render-background-color", "1, 1, 1" ) ;
        indigo.setOption( "render-base-color", "0.6, 0.6, 0.6" ) ;
        indigo.setOption( "render-image-width", 500 ) ;
        indigo.setOption( "render-bond-length", 100 ) ;
        indigo.setOption( "render-coloring", true ) ;
        indigo.setOption( "render-label-mode", "terminal-hetero" ) ;
        indigo.setOption( "standardize-stereo-from-coordinates", true ) ;
        indigo.setOption( "standardize-fix-direction-wedge-bonds", true ) ;
        indigo.setOption( "standardize-charges", true ) ;
        
        renderer.renderToFile( mol, "/Users/sandeep/temp/" + ChemSpiderClientTest.FORMULA + ".png" ) ;
    }
}
