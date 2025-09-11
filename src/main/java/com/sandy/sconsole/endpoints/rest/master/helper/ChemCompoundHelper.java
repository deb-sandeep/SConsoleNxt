package com.sandy.sconsole.endpoints.rest.master.helper;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.api.client.chemspider.ChemCompound;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderAPIClient;
import com.sandy.sconsole.core.api.client.chemspider.ChemSpiderException;
import com.sandy.sconsole.dao.chem.ChemCompoundDBO;
import com.sandy.sconsole.dao.chem.ChemCompoundRepo;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.ChemCompoundImportReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.sandy.sconsole.core.util.StringUtil.toJSON;
import static com.sandy.sconsole.endpoints.rest.master.vo.reqres.ChemCompoundImportReq.*;

@Slf4j
@Component
public class ChemCompoundHelper {
    
    @Autowired private ChemCompoundRepo ccRepo ;
    @Autowired private ChemSpiderAPIClient csClient ;
    @Autowired private SConsoleConfig config ;
    
    public ChemCompound importCompound( ChemCompoundImportReq req )
        throws ChemSpiderException, IOException {
        
        log.debug( "Importing compound: {}", toJSON( req ) ) ;
        ChemCompound cc = fetchCompoundFromAPI( req ) ;
        log.debug( "Fetched compound}" ) ;
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
    
    public void deleteCompoundFiles( int id ) {
        ChemCompoundDBO dbo = ccRepo.findById( id ).get() ;
        File imgDir = config.getChemCompoundsImgFolder() ;
        
        File darkImgFile = new File( imgDir, dbo.getChemSpiderId() + ".dark.png" ) ;
        File lightImgFile = new File( imgDir, dbo.getChemSpiderId() + ".light.png" ) ;
        
        if( darkImgFile.delete() ) {
            log.debug( "Deleted dark image file: {}", darkImgFile.getAbsolutePath() ) ;
        }
        else {
            log.warn( "Failed to delete dark image file: {}", darkImgFile.getAbsolutePath() ) ;
        }
        
        if( lightImgFile.delete() ) {
            log.debug( "Deleted light image file: {}", lightImgFile.getAbsolutePath() ) ;
        }
        else {
            log.warn( "Failed to delete light image file: {}", lightImgFile.getAbsolutePath() ) ;
        }
    }
    
    public Path prepareArchiveForDownload( List<Integer> ids ) throws Exception {
        File archiveFile = new File( config.getWorkspacePath(), "anki-import.zip" ) ;
        try( ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( archiveFile ) ) ) {
            List<ChemCompoundDBO> dbos = ccRepo.findAllById( ids ) ;
            addImportTxtFileToArchive( dbos, zos ) ;
            for( ChemCompoundDBO dbo : dbos ) {
                addChemCompoundImgToArchive( dbo, zos ) ;
                dbo.setCardDownloaded( true );
            }
            ccRepo.saveAll( dbos ) ;
            return archiveFile.toPath() ;
        }
    }
    
    private void addChemCompoundImgToArchive( ChemCompoundDBO cc, ZipOutputStream zos )
            throws Exception {
        
        File imgFile = new File( config.getChemCompoundsImgFolder(),
                                 cc.getChemSpiderId() + ".dark.png" ) ;
        String zipEntryName = "anki-import/images/" + cc.getChemSpiderId() + ".dark.png" ;
        if( imgFile.exists() ) {
            zos.putNextEntry( new ZipEntry( zipEntryName ) ) ;
            zos.write( FileUtils.readFileToByteArray( imgFile ) ) ;
            zos.closeEntry() ;
        }
        else {
            log.warn( "No dark image file found for compound: {}", cc.getChemSpiderId() ) ;
        }
    }
    
    private void addImportTxtFileToArchive( List<ChemCompoundDBO> dbos, ZipOutputStream zos )
            throws Exception {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "#separator:;\n" ) ;
        sb.append( "#html:true\n" ) ;
        sb.append( "#notetype:Molecular Structure\n" ) ;
        sb.append( "#deck:Molecular Structure\n" ) ;
        sb.append( "#columns:CommonName;Formula;ChemSpiderID\n" ) ;
        for( ChemCompoundDBO dbo : dbos ) {
            sb.append( dbo.getCommonName() ).append( ";" ) ;
            sb.append( dbo.getCompactFormula() ).append( ";" ) ;
            sb.append( dbo.getChemSpiderId() ).append( "\n" ) ;
        }
        
        zos.putNextEntry( new ZipEntry( "anki-import/import.txt" ) ) ;
        zos.write( sb.toString().getBytes() ) ;
        zos.closeEntry() ;
    }
}
