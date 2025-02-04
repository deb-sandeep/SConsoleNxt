package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.vo.ChapterProblemsTopicMappingVO;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.master.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/ProblemTopicMapping" )
public class ProblemTopicMappingAPIs {
    
    @Autowired private ChapterRepo chapterRepo;
    
    @Autowired private ProblemRepo problemRepo ;
    
    @Autowired private SyllabusRepo syllabusRepo ;
    
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
}
