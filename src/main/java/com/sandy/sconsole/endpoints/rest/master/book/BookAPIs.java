package com.sandy.sconsole.endpoints.rest.master.book;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.ChapterId;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.repo.BookRepo;
import com.sandy.sconsole.dao.master.repo.ChapterRepo;
import com.sandy.sconsole.dao.master.repo.SyllabusBookMapRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.endpoints.rest.master.book.helper.BookHelper;
import com.sandy.sconsole.endpoints.rest.master.book.helper.TopicMappingHelper;
import com.sandy.sconsole.endpoints.rest.master.book.vo.BookProblemSummaryVO;
import com.sandy.sconsole.endpoints.rest.master.book.vo.BookTopicMappingVO;
import com.sandy.sconsole.endpoints.rest.master.book.vo.reqres.AttrChangeReq;
import com.sandy.sconsole.endpoints.rest.master.book.vo.reqres.BookTopicMappingRes;
import com.sandy.sconsole.endpoints.rest.master.core.vo.ChapterVO;
import com.sandy.sconsole.endpoints.rest.master.core.vo.TopicVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sandy.sconsole.core.api.AR.*;
import static java.text.MessageFormat.format;

@Slf4j
@RestController
@RequestMapping( "/Master/Book" )
public class BookAPIs {
    
    @Autowired private ChapterRepo chapterRepo;
    @Autowired private BookRepo bookRepo;
    @Autowired private SyllabusBookMapRepo sbmRepo = null ;
    @Autowired private TopicRepo topicRepo;
    @Autowired private BookHelper bookHelper = null ;
    @Autowired private TopicMappingHelper helper = null ;

    @GetMapping( "Listing" )
    public ResponseEntity<AR<List<BookRepo.BookSummary>>> getBookListing() {
        try {
            return success( bookHelper.getAllBookSummaries() ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @GetMapping( "{bookId}/ProblemSummary" )
    public ResponseEntity<AR<BookProblemSummaryVO>> getProblemsSummary(
            @PathVariable( "bookId" ) int bookId ) {
    
        try {
            return success( bookHelper.getBookProblemsSummary( bookId ) ) ;
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
    @GetMapping( "TopicMappings" )
    public ResponseEntity<AR<BookTopicMappingRes>> getBookTopicMappings(
            @RequestParam( "bookIds" ) Integer[] bookIds ) {
        
        try {
            List<BookTopicMappingVO> btmVOList ;
            Syllabus                 syllabus ;
            List<TopicVO>            topics = new ArrayList<>() ;
            
            if( bookIds == null || bookIds.length == 0 ) {
                return functionalError( "No books specified" ) ;
            }
            
            syllabus = sbmRepo.findByBookId( bookIds[0] ) ;
            btmVOList = helper.getBookTopicMappings( bookIds, syllabus ) ;
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
    
    @PostMapping( "{bookId}/UpdateAttribute" )
    @Transactional
    public ResponseEntity<AR<String>> updateBookAttribute (
            @PathVariable( "bookId" ) Integer bookId,
            @RequestBody AttrChangeReq request ) {
        
        try {
            Book book = bookRepo.findById( bookId ).get() ;
            
            Class<?> propertyType = PropertyUtils.getPropertyType( book, request.getAttribute() ) ;
            Object convertedValue = ConvertUtils.convert( request.getValue(), propertyType ) ;
            PropertyUtils.setProperty( book, request.getAttribute(), convertedValue ) ;
            
            bookRepo.save( book ) ;
            
            return success( format( "Attribute {0} updated to {1} for book {2}",
                                    request.getAttribute(),
                                    request.getValue(),
                                    book.getBookShortName() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }

    @PostMapping( "{bookId}/{chapterNum}/SaveChapterName" )
    @Transactional
    public ResponseEntity<AR<ChapterVO>> saveChapterName (
            @PathVariable( "bookId" ) Integer bookId,
            @PathVariable( "chapterNum" ) Integer chapterNum,
            @RequestBody AttrChangeReq request ) {
        
        try {
            ChapterId chapterId = new ChapterId( bookId, chapterNum ) ;
            Optional<Chapter> chOpt = chapterRepo.findById( chapterId ) ;
            
            Chapter ch ;
            
            if( chOpt.isPresent() ) {
                ch = chOpt.get() ;
            }
            else {
                ch = new Chapter() ;
                ch.setId( chapterId ) ;
                ch.setBook( bookRepo.findById( bookId ).get() ) ;
            }
            
            ch.setChapterName( request.getValue() ) ;
            
            Chapter savedChapter = chapterRepo.save( ch ) ;
            
            return success( new ChapterVO( savedChapter ) ) ;
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
}
