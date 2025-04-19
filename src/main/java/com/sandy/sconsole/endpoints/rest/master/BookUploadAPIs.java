package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.endpoints.rest.master.helper.BookHelper;
import com.sandy.sconsole.endpoints.rest.master.helper.BookMetaValidator;
import com.sandy.sconsole.endpoints.rest.master.helper.BookUploadHelper;
import com.sandy.sconsole.endpoints.rest.master.vo.BookMetaVO;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.CreateNewExerciseReq;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.SaveBookMetaReq;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.SaveBookMetaRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/Book" )
public class BookUploadAPIs {
    
    @Autowired private BookUploadHelper bookHelper = null ;
    @Autowired private BookMetaValidator metaValidator = null ;
    
    @PostMapping( "/ValidateMetaFile" )
    public ResponseEntity<AR<BookMetaVO>> validateMetaFile(
            @RequestParam( "file" ) MultipartFile multipartFile ) {
        try {
            File savedFile = bookHelper.saveUploadedFile( multipartFile ) ;
            BookMetaVO meta = bookHelper.parseAndValidateBookMeta( savedFile ) ;
            
            meta.setServerFileName( savedFile.getName() );
            
            return success( meta ) ;
        }
        catch( BookHelper.InvalidMetaFileException ife ) {
            return functionalError( ife.getMessage(), ife.getCause() ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "/SaveMeta" )
    public ResponseEntity<AR<SaveBookMetaRes>> saveMetaFile(
            @RequestBody SaveBookMetaReq request ) {
        
        try {
            String uploadedFileName = request.getUploadedFileName() ;
            File savedFile = bookHelper.getUploadedFile( uploadedFileName ) ;
            if( !savedFile.exists() ) {
                return badRequest( "Uploaded file " + uploadedFileName + " does not exist." ) ;
            }

            BookMetaVO meta = bookHelper.parseAndValidateBookMeta( savedFile ) ;
            if( meta.getTotalMsgCount().getNumError() > 0 ) {
                return functionalError( "Cannot save book meta with errors." ) ;
            }
            
            SaveBookMetaRes response = bookHelper.saveBookMeta( meta ) ;
            return success( response ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "/CreateNewExercise" )
    public ResponseEntity<AR<List<String>>> createNewExercise(
            @RequestBody CreateNewExerciseReq request ) {
        
        try {
            return success( bookHelper.createNewExercise( request ) ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
