package com.sandy.sconsole.api.master.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sandy.sconsole.core.SConsoleConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class BookAPIHelper {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyyMMddHHmmss" ) ;
    
    @Autowired SConsoleConfig config ;
    @Autowired BookMetaValidator validator ;
    
    private File uploadDir = null ;
    
    private File getUploadFolder() {
        if( uploadDir == null ) {
            uploadDir = new File( config.getWorkspacePath(), "uploads" ) ;
            if( !uploadDir.exists() ) {
                if( !uploadDir.mkdirs() ) {
                    log.error( "Uploads directory could not be created at " + uploadDir.getAbsolutePath() );
                }
            }
        }
        return uploadDir ;
    }
    
    public File saveUploadedFile( MultipartFile mFile ) throws Exception {
        
        String destFileName = mFile.getOriginalFilename() + "." + SDF.format( new Date() );
        File destFile = new File( getUploadFolder(), destFileName ) ;
        mFile.transferTo( destFile ) ;
        return destFile ;
    }
    
    public BookMeta parseAndValidateBookMeta( File metaFile ) throws Exception {
        
        ObjectMapper mapper = new ObjectMapper( new YAMLFactory() ) ;
        mapper.findAndRegisterModules() ;
        
        BookMeta meta = mapper.readValue( metaFile, BookMeta.class );
        validator.validateBookMeta( meta ) ;
        
        return meta ;
    }
    

}
