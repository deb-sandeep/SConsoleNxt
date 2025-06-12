package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.master.repo.ChapterRepo;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import com.sandy.sconsole.endpoints.rest.master.helper.TopicMappingHelper;
import com.sandy.sconsole.endpoints.rest.master.vo.ChapterProblemsTopicMappingDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.EventCatalog.TOPIC_PROBLEM_ASSOCIATION_UPDATED;
import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/ProblemTopicMapping" )
public class ProblemTopicMappingAPIs {
    
    @Autowired private EventBus eventBus;
    
    @Autowired private ChapterRepo chapterRepo;
    
    @Autowired private ProblemRepo problemRepo ;
    
    @Autowired private SyllabusRepo syllabusRepo ;
    
    @Autowired private TopicMappingHelper helper ;
    
    @Autowired private PlatformTransactionManager txnMgr ;
    
    private TransactionTemplate txTemplate ;
    
    @PostConstruct
    public void init() {
        this.txTemplate = new TransactionTemplate( txnMgr ) ;
    }
    
    @GetMapping( "Book/{bookId}/Chapter/{chapterNum}" )
    public ResponseEntity<AR<ChapterProblemsTopicMappingDTO>> getProblemTopicMappingsForChapter(
                                @PathVariable( "bookId" ) int bookId,
                                @PathVariable( "chapterNum" ) int chapterNum ) {
    
        try {
            ChapterProblemsTopicMappingDTO result ;
            
            Chapter chapter = chapterRepo.findById( new ChapterId( bookId, chapterNum ) ).get() ;
            Syllabus syllabus = syllabusRepo.findBySubjectName( chapter.getBook().getSubjectName() ).get( 0 ) ;
            
            List<Object[]> records = this.problemRepo.getProblemTopicMappings( bookId, chapterNum ) ;
            
            result = new ChapterProblemsTopicMappingDTO( chapter, syllabus ) ;
            
            records.forEach( record -> {
                Problem p = ( Problem )record[0] ;
                TopicChapterProblemMap tcmForProblem = ( TopicChapterProblemMap )record[1] ;

                result.addProblemMapping( p, tcmForProblem ) ;
            } ) ;
            
            return success( result ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "AttachProblems/{topicChapterMapId}")
    public ResponseEntity<AR<Map<Integer, Integer>>> attachProblems(
            @PathVariable( "topicChapterMapId" ) int topicChapterMapId,
            @RequestBody() Integer[] problemIds ) {
        
        try {
            // Key is problem id, value is topic_chapter_problem_map id
            Map<Integer, Integer> result = txTemplate.execute( status ->
                    helper.linkProblemsToTopicChapterMapping( topicChapterMapId, problemIds ) ) ;
            eventBus.publishEvent( TOPIC_PROBLEM_ASSOCIATION_UPDATED ) ;
            return success( result ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "DetachProblems" )
    @Transactional
    public ResponseEntity<AR<String>> detachProblems( @RequestBody() Integer[] problemIds ) {
        
        try {
            txTemplate.executeWithoutResult( status -> helper.unlinkProblems( problemIds ) );
            eventBus.publishEvent( TOPIC_PROBLEM_ASSOCIATION_UPDATED ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
