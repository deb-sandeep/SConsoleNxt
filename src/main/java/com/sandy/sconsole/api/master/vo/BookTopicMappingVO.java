package com.sandy.sconsole.api.master.vo;

import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.TopicChapterMap;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookTopicMappingVO {
    
    @Data
    public static class TopicMappingVO {
        int mappingId ;
        int topicId ;
        
        public TopicMappingVO( TopicChapterMap tcm ) {
            this.mappingId = tcm.getId() ;
            this.topicId = tcm.getTopic().getId() ;
        }
    }
    
    @Data
    public static class ChapterTopicMappingVO {
        int chapterNum ;
        String chapterName ;
        List<TopicMappingVO> topics = new ArrayList<>();
        
        public ChapterTopicMappingVO( Chapter chapter ) {
            this.chapterNum = chapter.getId().getChapterNum() ;
            this.chapterName = chapter.getChapterName() ;
        }
        
        public void addTopicMapping( TopicChapterMap tcm ) {
            this.topics.add( new TopicMappingVO( tcm ) ) ;
        }
    }
    
    private BookVO book ;
    private List<ChapterTopicMappingVO> chapterTopicMappings = new ArrayList<>() ;
    
    public BookTopicMappingVO( Book book, Syllabus syllabus ) {
        this.book = new BookVO( book ) ;
        this.book.setSyllabusName( syllabus.getSyllabusName() ) ;
    }
    
    public void addChapterTopicMapping( Chapter c, TopicChapterMap tcm ) {
        ChapterTopicMappingVO ctm = getChapterTopicMappingVO( c ) ;
        if( tcm != null ) {
            ctm.addTopicMapping( tcm ) ;
        }
    }
    
    private ChapterTopicMappingVO getChapterTopicMappingVO( Chapter c ) {
        for( ChapterTopicMappingVO ctm : chapterTopicMappings ) {
            if( ctm.getChapterNum() == c.getId().getChapterNum() ) {
                return ctm ;
            }
        }
        
        ChapterTopicMappingVO ctm = new ChapterTopicMappingVO( c ) ;
        chapterTopicMappings.add( ctm ) ;
        return ctm ;
    }
}
