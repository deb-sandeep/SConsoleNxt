package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.dto.*;
import com.sandy.sconsole.api.master.helper.BookAPIHelper;
import com.sandy.sconsole.api.master.helper.ChapterTopicMappingHelper;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.ChapterId;
import com.sandy.sconsole.dao.master.repo.BookRepo;
import com.sandy.sconsole.dao.master.repo.ChapterRepo;
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
    private ChapterRepo chapterRepo;
    
    @Autowired
    private BookRepo bookRepo;
    
    @Autowired
    private SyllabusRepo syllabusRepo = null ;
    
    @Autowired
    private BookAPIHelper bookHelper = null ;
    
    @Autowired
    private ChapterTopicMappingHelper chapterTopicMappingHelper = null ;
    
    @GetMapping( "/Listing" )
    public ResponseEntity<AR<List<BookSummary>>> getBookListing() {
        try {
            return success( bookHelper.getAllBookSummaries() ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @GetMapping( "{bookId}/ProblemSummary" )
    public ResponseEntity<AR<BookProblemSummary>> getProblemsSummary(
            @PathVariable( "bookId" ) int bookId ) {
    
        try {
            return success( bookHelper.getBookProblemsSummary( bookId ) ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "/ValidateMetaFile" )
    public ResponseEntity<AR<BookMeta>> validateMetaFile(
            @RequestParam( "file" ) MultipartFile multipartFile ) {
        try {
            File savedFile = bookHelper.saveUploadedFile( multipartFile ) ;
            BookMeta meta = bookHelper.parseAndValidateBookMeta( savedFile ) ;
            
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
            File savedFile = bookHelper.getUploadedFile( uploadedFileName ) ;
            if( !savedFile.exists() ) {
                return badRequest( "Uploaded file " + uploadedFileName + " does not exist." ) ;
            }

            BookMeta meta = bookHelper.parseAndValidateBookMeta( savedFile ) ;
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
    
    @PostMapping( "{bookId}/UpdateAttribute" )
    public ResponseEntity<AR<String>> updateBookAttribute (
            @PathVariable( "bookId" ) Integer bookId,
            @RequestBody AttrChangeRequest request ) {
        
        try {
            Book book = bookRepo.findById( bookId ).get() ;
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

    @PostMapping( "{bookId}/{chapterNum}/UpdateChapterName" )
    public ResponseEntity<AR<String>> updateChapterName (
            @PathVariable( "bookId" ) Integer bookId,
            @PathVariable( "chapterNum" ) Integer chapterNum,
            @RequestBody AttrChangeRequest request ) {
        
        try {
            ChapterId chapterId = new ChapterId( bookId, chapterNum ) ;
            Chapter ch = chapterRepo.findById( chapterId ).get() ;
            
            ch.setChapterName( request.getValue() ) ;
            
            chapterRepo.save( ch ) ;
            
            return success( format( "Chapter name updated to '{1}'",
                                    request.getValue() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }

    @PostMapping( "{bookId}/{chapterNum}/{exerciseNum}/UpdateExerciseName" )
    public ResponseEntity<AR<String>> updateExerciseName (
                @PathVariable( "bookId" ) Integer bookId,
                @PathVariable( "chapterNum" ) Integer chapterNum,
                @PathVariable( "exerciseNum" ) Integer exerciseNum,
                @RequestBody AttrChangeRequest request ) {
        
        try {
            int numProblemsUpdated = bookHelper.updateExerciseName( bookId,
                                                                chapterNum,
                                                                exerciseNum,
                                                                request.getValue() ) ;
            return success( format( "{0} problems updated", numProblemsUpdated ) ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PutMapping( "/ChapterTopicMapping" )
    public ResponseEntity<AR<String>> createOrUpdateChapterTopicMapping(
            @RequestBody ChapterTopicMappingReq mappingReq ) {
        
        try {
            chapterTopicMappingHelper.createOrUpdateMapping( mappingReq ) ;
            return success( "Chapter topic mapping successful" ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @DeleteMapping( "/ChapterTopicMapping/{mapId}" )
    public ResponseEntity<AR<String>> deleteChapterTopicMapping(
            @PathVariable( "mapId" ) Integer mapId ) {
        
        try {
            chapterTopicMappingHelper.deleteMapping( mapId ) ;
            return success( "Chapter topic mapping deleted successfully" );
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
