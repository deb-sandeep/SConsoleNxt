package com.sandy.sconsole.api.master.vo;

import com.sandy.sconsole.dao.master.TopicChapterMap;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
        
        ChapterMapping( TopicChapterMap tcm ) {
            this.mappingId          = tcm.getId() ;
            this.attemptSeq         = tcm.getAttemptSeq() ;
            this.bookId             = tcm.getChapter().getBook().getId() ;
            this.bookShortName      = tcm.getChapter().getBook().getBookShortName() ;
            this.chapterNum         = tcm.getChapter().getId().getChapterNum() ;
            this.chapterName        = tcm.getChapter().getChapterName() ;
            this.problemMappingDone = tcm.getProblemMappingDone() ;
        }
    }
    
    private int topicId ;
    private String topicName ;
    private String topicSection ;
    private String syllabusName ;
    private List<ChapterMapping> chapters ;
    
    public TopicChapterMappingVO( TopicChapterMap tcm ) {
        this.topicId      = tcm.getTopic().getId() ;
        this.topicName    = tcm.getTopic().getTopicName() ;
        this.topicSection = tcm.getTopic().getSectionName() ;
        this.syllabusName = tcm.getTopic().getSyllabus().getSyllabusName() ;
        this.chapters     = new ArrayList<>() ;
        
        addChapter( tcm ) ;
    }
    
    public void addChapter( TopicChapterMap tcm ) {
        this.chapters.add( new ChapterMapping( tcm ) ) ;
    }
}
