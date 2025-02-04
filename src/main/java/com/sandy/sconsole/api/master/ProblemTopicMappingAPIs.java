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
    
    @Autowired private TopicRepo topicRepo ;
    
    @Autowired private TopicChapterMapRepo tcmRepo ;
    
    @GetMapping( "{topicChapterMappingId}" )
    public ResponseEntity<AR<ChapterProblemsTopicMappingVO>> getProblemTopicMappings(
                                @PathVariable( "topicChapterMappingId" ) int topicChapterMappingId ) {
    
        try {
            ChapterProblemsTopicMappingVO result ;
            
            TopicChapterMap tcm = tcmRepo.findById( topicChapterMappingId ).get() ;
            
            Chapter chapter = tcm.getChapter() ;
            Topic topic = tcm.getTopic() ;
            Syllabus syllabus = syllabusRepo.findBySubject( chapter.getBook().getSubject() ).get( 0 ) ;
            
            int bookId = tcm.getChapter().getBook().getId() ;
            int chapterNum = tcm.getChapter().getId().getChapterNum() ;
            int selTopicId = tcm.getTopic().getId() ;
            
            List<Object[]> records = this.problemRepo.getProblemTopicMappings( bookId, chapterNum ) ;
            
            result = new ChapterProblemsTopicMappingVO( chapter, syllabus, topic ) ;
            
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
    
    // Attach, detach, force attach, etc.
}
