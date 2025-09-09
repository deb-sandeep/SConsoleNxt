package com.sandy.sconsole.endpoints.rest.master.helper;

import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderAPIClient;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderException;
import com.sandy.sconsole.dao.chem.ChemCompoundDBO;
import com.sandy.sconsole.dao.chem.ChemCompoundRepo;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.ChemCompoundImportReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.sandy.sconsole.core.util.StringUtil.toJSON;
import static com.sandy.sconsole.endpoints.rest.master.vo.reqres.ChemCompoundImportReq.*;

@Slf4j
@Component
public class ChemCompoundHelper {
    
    @Autowired private ChemCompoundRepo ccRepo ;
    @Autowired private ChemSpiderAPIClient csClient ;
    
    public ChemCompound importCompound( ChemCompoundImportReq req )
        throws ChemSpiderException, IOException {
        
        log.debug( "Importing compound: {}", toJSON( req ) ) ;
        ChemCompound cc = fetchCompoundFromAPI( req ) ;
        log.debug( "Fetched compound: {}", toJSON( cc ) ) ;
        if( cc != null ) {
            ChemCompoundDBO dbo = ccRepo.findBySmiles( cc.getSmiles() ) ;
            if( dbo != null ) {
                log.debug( "Local copy already present." ) ;
                if( !req.isForceImport() ) {
                    log.debug( "Not importing. ForceImport flag is false." ) ;
                    throw new ChemSpiderException( "Local copy already present. Not importing" );
                }
                else {
                    log.debug( "ForceImport flag is true. Overwriting local copy." ) ;
                    dbo.mergeValues( cc ) ;
                }
            }
            else {
                dbo = new ChemCompoundDBO( cc ) ;
            }
            log.debug( "Rendering molecule." ) ;
            csClient.renderMol2D( cc ) ;
            
            log.debug( "Persisting local copy." ) ;
            ChemCompoundDBO savedDBO = ccRepo.save( dbo ) ;
            cc.setId( savedDBO.getId() ) ;
        }
        return cc ;
    }
    
    private ChemCompound fetchCompoundFromAPI( ChemCompoundImportReq req )
        throws ChemSpiderException {
        return switch( req.getImportType() ) {
            case IMPORT_TYPE_FORMULA -> csClient.fetchByFormula( req.getFilterText() );
            case IMPORT_TYPE_NAME -> csClient.fetchByName( req.getFilterText() );
            case IMPORT_TYPE_SMILES -> csClient.fetchBySmiles( req.getFilterText() );
            default -> throw new ChemSpiderException( "Unexpected import type: " + req.getImportType() ) ;
        };
    }
}
