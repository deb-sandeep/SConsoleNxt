package com.sandy.sconsole.endpoints.rest.master.helper;

import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderAPIClient;
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
        throws IllegalStateException, IOException {
        
        log.debug( "Importing compound: {}", toJSON( req ) ) ;
        ChemCompound cc = fetchCompoundFromAPI( req ) ;
        log.debug( "Fetched compound: {}", toJSON( cc ) ) ;
        if( cc != null ) {
            ChemCompoundDBO dbo = ccRepo.findBySmiles( cc.getSmiles() ) ;
            if( dbo != null ) {
                log.debug( "Local copy already present." ) ;
                if( !req.isForceImport() ) {
                    log.debug( "Not importing. ForceImport flag is false." ) ;
                    throw new IllegalStateException( "Local copy already present. Not importing" );
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
            ccRepo.save( dbo ) ;
        }
        return cc ;
    }
    
    private ChemCompound fetchCompoundFromAPI( ChemCompoundImportReq req ) {
        return switch( req.getImportType() ) {
            case IMPORT_TYPE_FORMULA -> csClient.fetchByFormula( req.getFormula() );
            case IMPORT_TYPE_NAME -> csClient.fetchByName( req.getName() );
            case IMPORT_TYPE_SMILES -> csClient.fetchBySmiles( req.getSmiles() );
            default -> throw new IllegalStateException( "Unexpected import type: " + req.getImportType() ) ;
        };
    }
}
