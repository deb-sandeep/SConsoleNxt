package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.dto.SaveMetaRequest;
import com.sandy.sconsole.api.master.dto.SaveMetaResponse;
import com.sandy.sconsole.api.master.helper.BookAPIHelper;
import com.sandy.sconsole.api.master.dto.BookMeta;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static com.sandy.sconsole.core.api.AR.systemError;
import static com.sandy.sconsole.core.api.AR.badRequest;
import static com.sandy.sconsole.core.api.AR.success;

@Slf4j
@RestController
@RequestMapping( "/Master/Book" )
public class BookAPIs {
    
    @Autowired
    private SyllabusRepo syllabusRepo = null ;
    
    @Autowired
    private BookAPIHelper helper = null ;
    
    @PostMapping( "/ValidateMetaFile" )
    public ResponseEntity<AR<BookMeta>> validateMetaFile(
            @RequestParam( "file" ) MultipartFile multipartFile ) {
        try {
            File savedFile = helper.saveUploadedFile( multipartFile ) ;
            BookMeta meta = helper.parseAndValidateBookMeta( savedFile ) ;
            
            meta.setServerFileName( savedFile.getName() );
            
            return success( meta ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "/SaveMeta" )
    public ResponseEntity<AR<SaveMetaResponse>> saveMetaFile(
            @RequestBody SaveMetaRequest request ) {
        
        try {
            String uploadedFileName = request.getUploadedFileName() ;
            File savedFile = helper.getUploadedFile( uploadedFileName ) ;
            if( !savedFile.exists() ) {
                return badRequest( "Uploaded file " + uploadedFileName + " does not exist." ) ;
            }

            BookMeta meta = helper.parseAndValidateBookMeta( savedFile ) ;
            SaveMetaResponse response = helper.saveBookMeta( meta ) ;
            return success( response ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
