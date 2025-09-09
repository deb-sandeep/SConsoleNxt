package com.sandy.sconsole.core.api.client.chemspider;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.net.APIClient;
import com.sandy.sconsole.core.net.APIResponse;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class ChemSpiderAPIClient {
    
    private static final String FILTER_FORMULA_API =
            "https://api.rsc.org/compounds/v1/filter/formula" ;
    
    private static final String FILTER_NAME_API =
            "https://api.rsc.org/compounds/v1/filter/name" ;
    
    private static final String FILTER_SMILES_API =
            "https://api.rsc.org/compounds/v1/filter/smiles" ;
    
    private static final String QUERY_STATUS_API =
            "https://api.rsc.org/compounds/v1/filter/${qid}/status" ;
    
    private static final String QUERY_RESULT_ID_API =
            "https://api.rsc.org/compounds/v1/filter/${qid}/results" ;
    
    private static final String QUERY_RESULT_API =
            "https://api.rsc.org/compounds/v1/records/${rid}/details?" +
            "fields=SMILES,Formula,AverageMass,MolecularWeight,CommonName,Mol2D,Mol3D,StdInChIKey" ;
    
    private static final String PUBCHEM_IUPAC_NAME_API =
            "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/${SIK}/property/IUPACName/JSON" ;
    
    private Map<String, String> apiHeader ;
    private File imgFolder ;
    
    private final APIClient client ;
    
    public ChemSpiderAPIClient( APIClient client, SConsoleConfig config ) {
        this.client = client ;
        this.createApiHeaderMap( config ) ;
        this.createImgFolder( config ) ;
    }
    
    private void createApiHeaderMap( SConsoleConfig config ) {
        String apiKey = System.getProperty( "sconsole.chemSpiderApiKey" ) ;
        if( apiKey == null ) {
            apiKey = config.getChemSpiderApiKey() ;
        }
        if( apiKey == null ) {
            throw new IllegalArgumentException( "ChemSpider API key not specified. " +
                    "Please set the system property 'chemspider.api.secret'" ) ;
        }
        this.apiHeader = Map.of( "apiKey", apiKey,
                                 "Content-Type", "application/json" ) ;
    }
    
    private void createImgFolder( SConsoleConfig config ) {
        File imgDir = config.getChemCompoundsImgFolder() ;
        if( imgDir == null ) {
            File wkspDir = config.getWorkspacePath() ;
            if( wkspDir == null ) {
                wkspDir = new File( "/Users/sandeep/softwares/workspace/sconsole" ) ;
            }
            imgDir = new File( wkspDir, "chem-compound-imgs" ) ;
        }
        
        if( imgDir.exists() && !imgDir.isDirectory() ) {
            throw new IllegalArgumentException( "ChemCompoundsImgFolder is not a directory" ) ;
        }
        
        if( !imgDir.exists() ) {
            if( imgDir.mkdirs() ) {
                log.info( "Created ChemCompoundsImgFolder {}", imgDir.getAbsolutePath() ) ;
            }
        }
        
        this.imgFolder = imgDir ;
    }
    
    public ChemCompound fetchByName( String name ) {
        try {
            log.debug( "Fetching compound for name {}", name ) ;
            String queryId = getQueryId( FILTER_NAME_API, Map.of( "name", name) ) ;
            return fetchCompound( queryId ) ;
        }
        catch( Exception e ) {
            log.error( "  Error while fetching compound for name {}", name, e ) ;
        }
        return null ;
    }
    
    
    public ChemCompound fetchByFormula( String formula ) {
        try {
            log.debug( "Fetching compound for formula {}", formula ) ;
            String queryId = getQueryId( FILTER_FORMULA_API, Map.of( "formula", formula ) ) ;
            return fetchCompound( queryId ) ;
        }
        catch( Exception e ) {
            log.error( "  Error while fetching compound for formula {}", formula, e ) ;
        }
        return null ;
    }
    
    public ChemCompound fetchBySmiles( String smiles ) {
        try {
            log.debug( "Fetching compound for smiles {}", smiles ) ;
            String queryId = getQueryId( FILTER_SMILES_API, Map.of( "smiles", smiles ) ) ;
            return fetchCompound( queryId ) ;
        }
        catch( Exception e ) {
            log.error( "  Error while fetching compound for smiles {}", smiles, e ) ;
        }
        return null ;
    }
    
    private String getQueryId( String filterUrl, Map<String, String> bodyMap ) throws Exception {
        
        log.debug( "  Fetching query id" ) ;
        APIResponse response = client.post( filterUrl, apiHeader, bodyMap ) ;
        if( response.code() != 200 ) {
            log.error( response.body() ) ;
            throw new Exception( "Error while getting query id for url " + filterUrl + " with body " + bodyMap ) ;
        }
        
        JsonNode jsonNode = response.json() ;
        if( jsonNode.has( "queryId" ) ) {
            return jsonNode.get( "queryId" ).asText() ;
        }
        else {
            log.error( "\tResponse does not have queryId" ) ;
            log.error( response.body() ) ;
            throw new Exception( "Could not obtain queryId" ) ;
        }
    }
    
    private ChemCompound fetchCompound( String queryId ) throws  Exception {
        log.debug( "  Query id = {}", queryId ) ;
        
        waitTillQueryProcessed( queryId ) ;
        log.debug( "  Query processed on server" ) ;
        
        int resultId = getFirstResultId( queryId ) ;
        if( resultId != -1 ) {
            return fetchResultDetails( resultId ) ;
        }
        else {
            log.info( "  No compound found for query id {}", queryId ) ;
        }
        return null ;
    }
        
    
    private void waitTillQueryProcessed( String queryId ) throws Exception {
        
        log.debug( "  Waiting till query processed on server" ) ;
        
        final String url = QUERY_STATUS_API.replace( "${qid}", queryId ) ;
        final int NUM_ATTEMPTS = 5 ;
        int attempt = 0 ;
        
        Thread.sleep( 500 ) ;
        
        while( attempt < NUM_ATTEMPTS ) {
            APIResponse response = client.get( url, apiHeader ) ;
            if( response.code() != 200 ) {
                log.error( response.body() ) ;
                throw new Exception( "Error while getting query status for query id " + queryId ) ;
            }
            
            JsonNode jsonNode = response.json() ;
            if( jsonNode.has( "status" ) ) {
                String status = jsonNode.get( "status" ).asText() ;
                if( status.equalsIgnoreCase( "Complete" ) ) {
                    break ;
                }
                else {
                    log.debug( "\tQuery status = {} for query id {}", status, queryId ) ;
                    Thread.sleep( 1000 ) ;
                }
            }
            attempt++ ;
        }
    }
    
    private int getFirstResultId( String queryId ) throws Exception {
        
        APIResponse response = client.get( QUERY_RESULT_ID_API.replace( "${qid}", queryId ), apiHeader ) ;
        if( response.code() != 200 ) {
            log.error( response.body() ) ;
            throw new Exception( "Error while getting result id for query id " + queryId ) ;
        }
        
        JsonNode resultsNode = response.json().get( "results" ) ;
        if( resultsNode.isArray() && !resultsNode.isEmpty() ) {
            return resultsNode.get( 0 ).asInt() ;
        }
        return -1 ;
    }
    
    private ChemCompound fetchResultDetails( int resultId ) throws Exception {
        APIResponse response = client.get( QUERY_RESULT_API.replace( "${rid}", Integer.toString( resultId ) ), apiHeader ) ;
        if( response.code() != 200 ) {
            log.error( response.body() ) ;
            throw new Exception( "Error while getting result details for result id " + resultId ) ;
        }
        
        JsonNode jsonNode = response.json() ;
        ChemCompound cc = new ChemCompound() ;
        cc.setChemSpiderId( jsonNode.get( "id" ).asInt() ) ;
        cc.setCommonName( jsonNode.get( "commonName" ).asText() ) ;
        cc.setSmiles( jsonNode.get( "smiles" ).asText() ) ;
        cc.setFormula( jsonNode.get( "formula" ).asText() ) ;
        cc.setMolecularWeight( (float)jsonNode.get( "molecularWeight" ).asDouble() ) ;
        cc.setAverageMass( (float)jsonNode.get( "averageMass" ).asDouble() ) ;
        cc.setMol2D( jsonNode.get( "mol2D" ).asText() ) ;
        cc.setMol3D( jsonNode.get( "mol3D" ).asText() ) ;
        
        String stdInchiKey = jsonNode.get( "stdinchiKey" ).asText() ;
        if( stdInchiKey != null ) {
            cc.setIupacName( fetchIUPACName( stdInchiKey ) ) ;
        }
        
        return cc ;
    }
    
    private String fetchIUPACName( String stdInchiKey ) throws Exception {
        APIResponse response = client.get( PUBCHEM_IUPAC_NAME_API.replace( "${SIK}", stdInchiKey ) ) ;
        if( response.code() != 200 ) {
            log.error( response.body() ) ;
            throw new Exception( "Error while getting IUPAC name for inchi key " + stdInchiKey ) ;
        }
        
        JsonNode jsonNode = response.json() ;
        return jsonNode.at( "/PropertyTable/Properties/0/IUPACName" ).asText() ;
    }
    
    public void renderMol2D( ChemCompound cc ) throws IOException {
        log.debug( "  Rendering mol2D for compound {}", cc.getCommonName() ) ;
        if( cc.getMol2D() != null ) {
            Indigo indigo = new Indigo() ;
            
            IndigoObject mol = indigo.loadMolecule( cc.getMol2D() ) ;
            mol.layout() ;
            
            IndigoRenderer renderer = new IndigoRenderer( indigo ) ;
            indigo.setOption( "render-output-format", "png" ) ;
            indigo.setOption( "render-background-color", "1, 1, 1" ) ;
            indigo.setOption( "render-base-color", "0.6, 0.6, 0.6" ) ;
            indigo.setOption( "render-image-max-width", 500 ) ;
            indigo.setOption( "render-bond-length", 100 ) ;
            indigo.setOption( "render-coloring", true ) ;
            indigo.setOption( "render-label-mode", "terminal-hetero" ) ;
            indigo.setOption( "standardize-stereo-from-coordinates", true ) ;
            indigo.setOption( "standardize-fix-direction-wedge-bonds", true ) ;
            indigo.setOption( "standardize-charges", true ) ;
            
            byte[] pngBytes = renderer.renderToBuffer( mol ) ;
            BufferedImage lightImg = ImageIO.read( new ByteArrayInputStream( pngBytes ) ) ;
            BufferedImage darkImg = SwingUtils.invertColors( lightImg ) ;
            
            ImageIO.write( lightImg, "png", new File( this.imgFolder, cc.getChemSpiderId() + ".light.png" ) ) ;
            ImageIO.write( darkImg, "png", new File( this.imgFolder, cc.getChemSpiderId() + ".dark.png" ) ) ;
        }
        else {
            log.error( "No mol2D found for compound {}", cc.getCommonName() ) ;
        }
    }
}
