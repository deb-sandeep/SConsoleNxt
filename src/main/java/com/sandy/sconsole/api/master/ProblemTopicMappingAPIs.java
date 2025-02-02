package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.vo.ChapterProblemsTopicMappingVO;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.master.repo.ChapterRepo;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
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
    
    @Autowired private SyllabusRepo syllabusRepo = null ;
    
    @GetMapping( "{bookId}/{chapterNum}" )
    public ResponseEntity<AR<ChapterProblemsTopicMappingVO>> getProblemTopicMappings(
                                @PathVariable( "bookId" ) int bookId,
                                @PathVariable( "chapterNum" ) int chapterNum ) {
    
        try {
            ChapterProblemsTopicMappingVO result ;
            List<Object[]> records = this.problemRepo.getProblemTopicMappings( bookId, chapterNum ) ;
            
            ChapterId chapterId = new ChapterId( bookId, chapterNum ) ;
            Chapter chapter = chapterRepo.findById( chapterId ).get() ;
            Syllabus syllabus = syllabusRepo.findBySubject( chapter.getBook().getSubject() ).get( 0 ) ;
            
            result = new ChapterProblemsTopicMappingVO( chapter, syllabus ) ;
            
            records.forEach( record -> {
                Problem p = ( Problem )record[0] ;
                TopicChapterProblemMap tcm = ( TopicChapterProblemMap )record[1] ;

                result.addProblemMapping( p, tcm.getTopicChapterMap() ) ;
            } ) ;
            
            return success( result ) ;
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
}
