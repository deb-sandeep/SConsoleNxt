package com.sandy.sconsole.endpoints.rest.master.compounds;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderException;
import com.sandy.sconsole.dao.chem.ChemCompoundDBO;
import com.sandy.sconsole.dao.chem.ChemCompoundRepo;
import com.sandy.sconsole.endpoints.rest.master.compounds.helper.ChemCompoundHelper;
import com.sandy.sconsole.endpoints.rest.master.compounds.vo.reqres.ChemCompoundImportReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/ChemCompound" )
public class ChemCompoundAPIs {
    
    @Autowired private ChemCompoundRepo   ccRepo ;
    @Autowired private ChemCompoundHelper helper ;
    
    @GetMapping( "/All" )
    public ResponseEntity<AR<List<ChemCompound>>> getAllChemCompounds () {
        try {
            List<ChemCompound> compounds = new ArrayList<>() ;
            ccRepo.findAllByOrderByCommonName().forEach( dbo -> {
                ChemCompound cc = new ChemCompound( dbo ) ;
                // When sending all compounds, do not send mol files to reduce
                // on size of response - they can be fetched on a need basis.
                cc.setMol2D( null ) ;
                cc.setMol3D( null ) ;
                compounds.add( cc ) ;
            } ) ;
            return success( compounds ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/{id}" )
    public ResponseEntity<AR<ChemCompound>> getChemCompound (
            @PathVariable( "id" ) int id ) {
        try {
            return success( new ChemCompound( ccRepo.findById( id ).get() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @DeleteMapping( "/{id}" )
    @Transactional
    public ResponseEntity<AR<String>> deleteChemCompound ( @PathVariable( "id" ) int id ) {
        try {
            helper.deleteCompoundFiles( id ) ;
            ccRepo.deleteById( id ) ;
            return success( "Success" ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    
    @PostMapping( "/Import" )
    @Transactional
    public ResponseEntity<AR<ChemCompound>> importChemCompound (
                                    @RequestBody ChemCompoundImportReq req ) {
        try {
            ChemCompound cc = helper.importCompound( req ) ;
            if( cc != null ) {
                return success( cc ) ;
            }
            return functionalError( "No compound found for given name." ) ;
        }
        catch( ChemSpiderException cse ) {
            return functionalError( cse.getMessage(), cse.getCause() ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }

    @PostMapping( "/Save" )
    @Transactional
    public ResponseEntity<AR<String>> importChemCompound (
                                    @RequestBody ChemCompound editedCompound ) {
        try {
            ChemCompoundDBO dbo = ccRepo.findById( editedCompound.getId() ).get() ;
            dbo.setCommonName( editedCompound.getCommonName() ) ;
            dbo.setIupacName( editedCompound.getIupacName() ) ;
            dbo.setCompactFormula( editedCompound.getCompactFormula() ) ;
            dbo.setCardDownloaded( false ) ;
            ccRepo.save( dbo ) ;
            
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "/DownloadCards" )
    public ResponseEntity<Resource> downloadCards ( @RequestBody List<Integer> ids ) {
        try {
            Path path = helper.prepareArchiveForDownload( ids ) ;
            Resource resource = new UrlResource( path.toUri() ) ;
            return ResponseEntity.ok()
                    .header( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
                    .header( HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition" )
                    .contentType( MediaType.APPLICATION_OCTET_STREAM )
                    .body( resource ) ;
        }
        catch( Exception e ) {
            log.error( "Error while downloading cards.", e ) ;
            return ResponseEntity.status( 500 ).build() ;
        }
    }
}
