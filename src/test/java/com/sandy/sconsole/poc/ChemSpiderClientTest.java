package com.sandy.sconsole.poc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderAPIClient;
import com.sandy.sconsole.core.net.APIClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChemSpiderClientTest {

    public static final String FORMULA = "C2H2O6" ;
    public static final String NAME = "ozone" ;
    
    public static void main( String[] args ) throws Exception {
        try {
            ChemSpiderAPIClient app = new ChemSpiderAPIClient( new APIClient(), new SConsoleConfig() ) ;
            //ChemCompound        cc  = app.fetchByFormula( FORMULA ) ;
            //ChemCompound cc = app.fetchByName( NAME ) ;
            ChemCompound cc = app.fetchBySmiles( "CCN(CC)CC(=O)Nc1c(C)cccc1C" ) ;
            log.debug( new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString( cc ) ) ;
            log.debug( "Done" ) ;
        }
        catch( Exception e ) {
            log.error( "Error while fetching compound", e ) ;
        }
        finally {
            System.exit( -1 );
        }
    }
}
