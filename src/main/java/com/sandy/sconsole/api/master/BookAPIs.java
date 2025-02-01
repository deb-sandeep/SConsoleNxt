package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.vo.*;
import com.sandy.sconsole.api.master.helper.BookAPIHelper;
import com.sandy.sconsole.api.master.helper.ChapterTopicMappingHelper;
import com.sandy.sconsole.api.master.vo.reqres.*;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.master.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;
import static java.text.MessageFormat.format;

@Slf4j
@RestController
@RequestMapping( "/Master/Book" )
public class BookAPIs {
    @Autowired
    private TopicRepo topicRepo;
    
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
    
    @Autowired
    private SyllabusBookMapRepo sbmRepo = null ;
    
    @Autowired
    private TopicChapterMapRepo tcmRepo = null ;
    
    @GetMapping( "/Listing" )
    public ResponseEntity<AR<List<BookRepo.BookSummary>>> getBookListing() {
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
            @RequestBody AttrChangeReq request ) {
        
        try {
            Book book = bookRepo.findById( bookId ).get() ;
            
            Class<?> propertyType = PropertyUtils.getPropertyType( book, request.getAttribute() ) ;
            Object convertedValue = ConvertUtils.convert( request.getValue(), propertyType ) ;
            PropertyUtils.setProperty( book, request.getAttribute(), convertedValue ) ;
            
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
            @RequestBody AttrChangeReq request ) {
        
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
                @RequestBody AttrChangeReq request ) {
        
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
    
    @PostMapping( "/ChapterTopicMapping" )
    public ResponseEntity<AR<Integer>> createOrUpdateChapterTopicMapping(
            @RequestBody ChapterTopicMappingReq mappingReq ) {
        
        try {
            int mappingId = chapterTopicMappingHelper.createOrUpdateMapping( mappingReq ) ;
            return success( mappingId ) ;
        }
        catch( DataIntegrityViolationException dive ) {
            log.error( "Duplicate entry.", dive ) ;
            return functionalError( "Entry already exists", dive ) ;
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
    
    /**
     * Assumption is that all the books are mapped to syllabus and the syllabus
     * of all the books provided as input are the same. Validation is not done
     * on the server side.
     */
    @GetMapping( "/TopicMappings" )
    public ResponseEntity<AR<BookTopicMappingRes>> getBookTopicMappings(
            @RequestParam( "bookIds" ) Integer[] bookIds ) {
        
        try {
            List<BookTopicMappingVO> btmVOList ;
            Syllabus syllabus ;
            List<TopicVO> topics = new ArrayList<>() ;
            
            if( bookIds == null || bookIds.length == 0 ) {
                return functionalError( "No books specified" ) ;
            }
            
            syllabus = sbmRepo.findByBookId( bookIds[0] ) ;
            btmVOList = chapterTopicMappingHelper.getBookTopicMappings( bookIds, syllabus ) ;
            topicRepo.findTopics( syllabus.getSyllabusName() )
                     .forEach( t -> topics.add( new TopicVO( t ) ) ) ;
            
            BookTopicMappingRes res = new BookTopicMappingRes() ;
            res.setSyllabusName( syllabus.getSyllabusName() ) ;
            res.setTopics( topics ) ;
            res.setBookTopicMappingList( btmVOList ) ;
            
            return success( res ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
