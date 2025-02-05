package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.helper.TopicMappingHelper;
import com.sandy.sconsole.api.master.vo.ChapterProblemsTopicMappingVO;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.master.repo.ChapterRepo;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/ProblemTopicMapping" )
public class ProblemTopicMappingAPIs {
    
    @Autowired private ChapterRepo chapterRepo;
    
    @Autowired private ProblemRepo problemRepo ;
    
    @Autowired private SyllabusRepo syllabusRepo ;
    
    @Autowired private TopicMappingHelper helper ;
    
    @GetMapping( "Book/{bookId}/Chapter/{chapterNum}" )
    public ResponseEntity<AR<ChapterProblemsTopicMappingVO>> getProblemTopicMappingsForChapter(
                                @PathVariable( "bookId" ) int bookId,
                                @PathVariable( "chapterNum" ) int chapterNum ) {
    
        try {
            ChapterProblemsTopicMappingVO result ;
            
            Chapter chapter = chapterRepo.findById( new ChapterId( bookId, chapterNum ) ).get() ;
            Syllabus syllabus = syllabusRepo.findBySubject( chapter.getBook().getSubject() ).get( 0 ) ;
            
            List<Object[]> records = this.problemRepo.getProblemTopicMappings( bookId, chapterNum ) ;
            
            result = new ChapterProblemsTopicMappingVO( chapter, syllabus ) ;
            
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
    @Transactional
    public ResponseEntity<AR<Map<Integer, Integer>>> attachProblems(
            @PathVariable( "topicChapterMapId" ) int topicChapterMapId,
            @RequestBody() Integer[] problemIds ) {
        
        try {
            // Key is problem id, value is topic_chapter_problem_map id
            return success( helper.linkProblemsToTopicChapterMapping( topicChapterMapId, problemIds ) ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @PostMapping( "DetachProblems")
    @Transactional
    public ResponseEntity<AR<String>> detachProblems( @RequestBody() Integer[] problemIds ) {
        
        try {
            helper.unlinkProblems( problemIds ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
