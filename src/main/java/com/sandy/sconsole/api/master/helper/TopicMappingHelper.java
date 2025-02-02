package com.sandy.sconsole.api.master.helper;

import com.sandy.sconsole.api.master.vo.*;
import com.sandy.sconsole.api.master.vo.reqres.ChapterTopicMappingReq;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.master.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TopicMappingHelper {
    @Autowired
    private ProblemRepo problemRepo;
    
    @Autowired BookRepo bookRepo ;
    @Autowired ChapterRepo chapterRepo ;
    @Autowired TopicRepo topicRepo ;
    @Autowired TopicChapterMapRepo tcmRepo ;
    @Autowired SyllabusBookMapRepo sbmRepo ;
    @Autowired TopicChapterProblemMapRepo tcpmRepo ;
    
    public int createOrUpdateMapping( ChapterTopicMappingReq req ) {
        
        TopicChapterMap map ;
        
        if( req.getMappingId() == -1 ) {
            map = new TopicChapterMap() ;
            Integer nextAttemptSeq = tcmRepo.getNextAttemptSequence( req.getTopicId() ) ;
            map.setAttemptSeq( nextAttemptSeq == null ? 1 : nextAttemptSeq ) ;
        }
        else {
            map = tcmRepo.findById( req.getMappingId() ).get() ;
        }
        
        ChapterId chapterId = new ChapterId( req.getBookId(), req.getChapterNum() ) ;
        Chapter ch = chapterRepo.findById( chapterId ).get() ;
        Topic topic = topicRepo.findById( req.getTopicId() ).get() ;
        
        map.setChapter( ch ) ;
        map.setTopic( topic ) ;
        map = tcmRepo.save( map ) ;
        
        associateUnallocatedProblems( req.getBookId(), req.getChapterNum(), map ) ;
        
        return map.getId() ;
    }
    
    private void associateUnallocatedProblems( int bookId, int chapterNum, TopicChapterMap tcm ) {
        
        List<Problem> problems ;
        
        problems = problemRepo.getUnassociatedProblemsForChapter( bookId, chapterNum ) ;
        if( !problems.isEmpty() ) {
            final List<TopicChapterProblemMap> tcpmList = new ArrayList<>() ;
            problems.forEach( p -> {
                TopicChapterProblemMap tcpm = new TopicChapterProblemMap() ;
                tcpm.setProblem( p ) ;
                tcpm.setTopicChapterMap( tcm ) ;
                tcpmList.add( tcpm ) ;
            } ) ;
            tcpmRepo.saveAll( tcpmList ) ;
        }
    }
    
    public void deleteMapping( Integer mapId ) {
        tcmRepo.deleteById( mapId ) ;
    }
    
    public List<BookTopicMappingVO> getBookTopicMappings( Integer[] bookIds, Syllabus syllabus ) {
        
        List<BookTopicMappingVO> btmList = new ArrayList<>() ;
        Map<Integer, BookTopicMappingVO> btmMap = new LinkedHashMap<>() ;
        
        List<Object[]> ctms = tcmRepo.getTopicMappingsForBooks( bookIds ) ;
        for( Object[] ctm : ctms ) {
            Chapter c = ( Chapter )ctm[0] ;
            TopicChapterMap tcm = ( TopicChapterMap )ctm[1] ;
            Book b = c.getBook() ;

            BookTopicMappingVO btmVO = btmMap.computeIfAbsent(
                    b.getId(),
                    bookId -> new BookTopicMappingVO( b, syllabus ) ) ;
            
            btmVO.addChapterTopicMapping( c, tcm ) ;
        }
        
        return new ArrayList<>( btmMap.values() ) ;
    }
}
