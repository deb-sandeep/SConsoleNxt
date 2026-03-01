package com.sandy.sconsole.endpoints.rest.master.book.vo;

import com.sandy.sconsole.dao.master.TopicChapterMap;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TopicChapterMappingVO {

    @Data
    public static class ChapterMapping {
        private int mappingId ;
        private int attemptSeq ;
        private int bookId ;
        private String bookShortName ;
        private int chapterNum ;
        private String chapterName ;
        private boolean problemMappingDone ;
        private Map<String, Integer> problemTypeCountMap = new HashMap<>() ;
        
        ChapterMapping( TopicChapterMap tcm ) {
            this.mappingId          = tcm.getId() ;
            this.attemptSeq         = tcm.getAttemptSeq() ;
            this.bookId             = tcm.getChapter().getBook().getId() ;
            this.bookShortName      = tcm.getChapter().getBook().getBookShortName() ;
            this.chapterNum         = tcm.getChapter().getId().getChapterNum() ;
            this.chapterName        = tcm.getChapter().getChapterName() ;
            this.problemMappingDone = tcm.getProblemMappingDone() ;
        }
        
        public int getProblemCount() {
            return problemTypeCountMap.values().stream().reduce( 0, Integer::sum ) ;
        }
    }
    
    private int topicId ;
    private String topicName ;
    private String topicSection ;
    private String syllabusName ;
    private List<ChapterMapping> chapters ;
    private Map<String, Integer> problemTypeCountMap = new HashMap<>() ;
    private int problemCount ;
    
    public TopicChapterMappingVO( TopicChapterMap tcm ) {
        this.topicId      = tcm.getTopic().getId() ;
        this.topicName    = tcm.getTopic().getTopicName() ;
        this.topicSection = tcm.getTopic().getSectionName() ;
        this.syllabusName = tcm.getTopic().getSyllabus().getSyllabusName() ;
        this.chapters     = new ArrayList<>() ;
    }
    
    public ChapterMapping addChapter( TopicChapterMap tcm ) {
        ChapterMapping cm = new ChapterMapping( tcm ) ;
        this.chapters.add( cm ) ;
        return cm ;
    }
    
    public void calculateProblemCounts() {
        chapters.forEach( ch -> ch.getProblemTypeCountMap()
                                  .forEach( ( problemType, count ) -> {
            int newCount = 0 ;
            if( problemTypeCountMap.containsKey( problemType ) ) {
                newCount += problemTypeCountMap.get( problemType ) + count ;
            }
            else {
                newCount = count ;
            }
            problemTypeCountMap.put( problemType, newCount ) ;
            problemCount += count ;
        } ) );
    }
}
