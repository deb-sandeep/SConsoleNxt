package com.sandy.sconsole.poc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderAPIClient;
import com.sandy.sconsole.core.net.APIClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;

@Slf4j
public class ChemSpiderClientTest {

    public static final String FORMULA = "C2H2O6" ;
    public static final String NAME = "ozone" ;
    
    public static void main( String[] args ) throws Exception {
        try {
            ChemSpiderAPIClient app = new ChemSpiderAPIClient( new APIClient(), new SConsoleConfig() ) ;
            ChemCompound        cc  = app.fetchByFormula( FORMULA ) ;
            //ChemCompound cc = app.fetchByName( NAME ) ;
            log.debug( new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString( cc ) ) ;
            log.debug( "Done" ) ;
            
            if( cc != null ) {
                FileUtils.write( new File( "/Users/sandeep/temp/" + cc.getChemSpiderId() + ".mol" ), cc.getMol2D(), "UTF-8" ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error while fetching compound", e ) ;
        }
        finally {
            System.exit( -1 );
        }
    }
}
