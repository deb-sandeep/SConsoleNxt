package com.sandy.sconsole.api.master.helper;

import com.sandy.sconsole.api.master.vo.BookTopicMappingVO;
import com.sandy.sconsole.api.master.vo.TopicChapterMappingVO;
import com.sandy.sconsole.api.master.vo.reqres.ChapterTopicMappingReq;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.master.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.sandy.sconsole.api.master.vo.TopicChapterMappingVO.ChapterMapping;

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
    
    public int saveChapterTopicMapping( ChapterTopicMappingReq req ) {
        
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
        
        linkAvailableChapterProblemsToTopic( map ) ;
        
        return map.getId() ;
    }
    
    /**
     * This function deletes any existing mapping of the specified problems and
     * links them to the specified topic chapter mapping id. This effectively
     * 'reassigns' a problem from one topic chapter mapping to another.
     * <p>
     * IMPORTANT: If this behavior needs to be restricted, it is assumed that
     * the verification is done at the client end.
     */
    public Map<Integer, Integer> linkProblemsToTopicChapterMapping(
                                int topicChapterMapId, Integer[] problemIds ) {
        
        // Delete any existing linkages of the problems, effectively
        // 'reassigning' the problems to the specified topic chapter mapping.
        unlinkProblems( problemIds ) ;
        
        TopicChapterMap tcm = tcmRepo.findById( topicChapterMapId ).get() ;
        
        final List<TopicChapterProblemMap> tcpmList = new ArrayList<>() ;
        for( Problem p : problemRepo.findAllById( Arrays.asList( problemIds ) ) ) {
            TopicChapterProblemMap tcpm = new TopicChapterProblemMap() ;
            tcpm.setProblem( p ) ;
            tcpm.setTopicChapterMap( tcm ) ;
            tcpmList.add( tcpm ) ;
        }
        
        Map<Integer, Integer> problemMappingIds = new LinkedHashMap<>() ;
        for( TopicChapterProblemMap tcpm : tcpmRepo.saveAll( tcpmList ) ) {
            problemMappingIds.put( tcpm.getProblem().getId(), tcpm.getId() ) ;
        }
        
        return problemMappingIds ;
    }
    
    public void unlinkProblems( Integer[] problemIds ) {
        tcpmRepo.deleteProblemMappings( problemIds ) ;
    }
    
    /**
     * Given a topic-chapter mapping, this method allocates all the available
     * (not allocated) problems from the chapter (from topic-chapter mapping)
     * to the topic.
     * <p>
     * This is called during the creation of a topic chapter mapping to
     * mass link the available problems from the chapter to the topic. The
     * assumption (and fact) is that majority of chapters have a one-one mapping
     * with topics. Any fine-tuning (moving problems from one chapter to
     * another topic) can be done via the user interface by force linking the
     * problems to a different topic.
     */
    private void linkAvailableChapterProblemsToTopic( TopicChapterMap tcm ) {
        
        List<Problem> problems ;
        
        final int bookId = tcm.getChapter().getBook().getId() ;
        final int chapterNum = tcm.getChapter().getId().getChapterNum() ;
        
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
    
    /**
     * Note that the associated problems {@link TopicChapterProblemMap} are
     * deleted by database referential integrity constraint of cascade delete,
     * hence no explicit action is needed in code.
     */
    public void deleteTopicChapterMapping( Integer mapId ) {
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
    
    public List<TopicChapterMappingVO> getTopicChapterMappings( String syllabusName ) {
        
        List<TopicChapterMappingVO> voList ;
        if( syllabusName == null ) {
            voList = createTCMVO( tcmRepo.getAllTopicChapterMappings() ) ;
        }
        else {
            voList = createTCMVO( tcmRepo.getTopicChapterMappingsForSyllabus( syllabusName ) ) ;
        }
        return voList ;
    }
    
    private List<TopicChapterMappingVO> createTCMVO( List<TopicChapterMap> tcmList ) {
        
        List<TopicChapterMappingVO> voList = new ArrayList<>() ;
        Map<Integer, ChapterMapping> cmMap = new LinkedHashMap<>() ;
        
        Topic                 lastTopic = null ;
        TopicChapterMappingVO currentVO = null ;
        ChapterMapping        cm ;
        
        for( int i=0; i<tcmList.size(); i++ ) {
            TopicChapterMap tcm = tcmList.get( i ) ;
            if( lastTopic == null || tcm.getTopic() != lastTopic ) {
                currentVO = new TopicChapterMappingVO( tcm ) ;
                voList.add( currentVO ) ;
            }
            cm = currentVO.addChapter( tcm ) ;
            lastTopic = tcm.getTopic() ;
            
            cmMap.put( cm.getMappingId(), cm ) ;
        }
        
        Integer[] mappingIds = cmMap.keySet().toArray( new Integer[0] ) ;
        tcpmRepo.getTopicChapterProblemTypeCounts( mappingIds )
                .forEach( count -> cmMap.get( count.getMappingId() )
                                        .getProblemTypeCountMap()
                                        .put( count.getProblemType(), count.getCount() ) ) ;
        
        voList.forEach( TopicChapterMappingVO::calculateProblemCounts );
        
        return voList ;
    }
}
