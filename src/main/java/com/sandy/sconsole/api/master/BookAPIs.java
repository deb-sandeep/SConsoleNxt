package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.dto.*;
import com.sandy.sconsole.api.master.helper.BookAPIHelper;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.repo.BookRepo;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;
import static java.text.MessageFormat.format;

@Slf4j
@RestController
@RequestMapping( "/Master/Book" )
public class BookAPIs {
    @Autowired
    private BookRepo bookRepo;
    
    @Autowired
    private SyllabusRepo syllabusRepo = null ;
    
    @Autowired
    private BookAPIHelper helper = null ;
    
    @GetMapping( "/Listing" )
    public ResponseEntity<AR<List<BookSummary>>> getBookListing() {
        try {
            return success( helper.getAllBookSummaries() ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @GetMapping( "{bookId}/ProblemSummary" )
    public ResponseEntity<AR<BookProblemSummary>> getProblemsSummary(
            @PathVariable( "bookId" ) int bookId ) {
    
        try {
            return success( helper.getBookProblemsSummary( bookId ) ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "/ValidateMetaFile" )
    public ResponseEntity<AR<BookMeta>> validateMetaFile(
            @RequestParam( "file" ) MultipartFile multipartFile ) {
        try {
            File savedFile = helper.saveUploadedFile( multipartFile ) ;
            BookMeta meta = helper.parseAndValidateBookMeta( savedFile ) ;
            
            meta.setServerFileName( savedFile.getName() );
            
            return success( meta ) ;
        }
        catch( BookAPIHelper.InvalidMetaFileException ife ) {
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
            File savedFile = helper.getUploadedFile( uploadedFileName ) ;
            if( !savedFile.exists() ) {
                return badRequest( "Uploaded file " + uploadedFileName + " does not exist." ) ;
            }

            BookMeta meta = helper.parseAndValidateBookMeta( savedFile ) ;
            if( meta.getTotalMsgCount().getNumError() > 0 ) {
                return functionalError( "Cannot save book meta with errors." ) ;
            }
            
            SaveBookMetaRes response = helper.saveBookMeta( meta ) ;
            return success( response ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "/UpdateAttribute" )
    public ResponseEntity<AR<String>> saveMetaFile(
            @RequestBody AttrChangeRequest request ) {
        
        try {
            Book book = bookRepo.findById( request.getId() ).get() ;
            PropertyUtils.setProperty( book, request.getAttribute(), request.getValue() ) ;
            bookRepo.save( book ) ;
            return success( format( "Attribute '{0}' updated to '{1}' for book '{2}'",
                                    request.getAttribute(),
                                    request.getValue(),
                                    book.getBookShortName() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
