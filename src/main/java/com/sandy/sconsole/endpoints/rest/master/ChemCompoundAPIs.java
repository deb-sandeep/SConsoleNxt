package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderException;
import com.sandy.sconsole.dao.chem.ChemCompoundDBO;
import com.sandy.sconsole.dao.chem.ChemCompoundRepo;
import com.sandy.sconsole.endpoints.rest.master.helper.ChemCompoundHelper;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.ChemCompoundImportReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/ChemCompound" )
public class ChemCompoundAPIs {
    
    @Autowired private ChemCompoundRepo ccRepo ;
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
            ccRepo.save( dbo ) ;
            
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
