package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderException;
import com.sandy.sconsole.endpoints.rest.master.helper.ChemCompoundHelper;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.ChemCompoundImportReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/ChemCompound" )
public class ChemCompoundAPIs {
    
    @Autowired private ChemCompoundHelper helper ;
    
    @PostMapping( "/Import" )
    @Transactional
    public ResponseEntity<AR<ChemCompound>> importChemCompound (
                                    @RequestBody ChemCompoundImportReq req ) {
        try {
            log.debug( "/Master/ChemCompound/Import called with req: " + req.toString() ) ;
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
}
