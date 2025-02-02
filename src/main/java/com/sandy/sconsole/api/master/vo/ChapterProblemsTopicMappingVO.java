package com.sandy.sconsole.api.master.vo;

import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.TopicChapterMap;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChapterProblemsTopicMappingVO {
    
    @Data
    public static class ProblemTopicMapping {
        
        private ProblemVO problem = null ;
        private int mappingId = -1 ;
        private TopicVO topic = null ;
    }

    private BookVO book ;
    private int chapterNum ;
    private String chapterName ;
    private List<ProblemTopicMapping> problems = new ArrayList<>();
    
    public ChapterProblemsTopicMappingVO( Chapter chapter, Syllabus syllabus ) {
        this.book = new BookVO( chapter.getBook() ) ;
        this.book.setSyllabusName( syllabus.getSyllabusName() ) ;
        this.chapterNum = chapter.getId().getChapterNum() ;
        this.chapterName = chapter.getChapterName() ;
    }
    
    public void addProblemMapping( Problem problem, TopicChapterMap tcm ) {
        ProblemTopicMapping ptm = new ProblemTopicMapping() ;
        ptm.setProblem( new ProblemVO( problem ) ) ;
        if( tcm != null ) {
            ptm.setMappingId( tcm.getId() ) ;
            ptm.setTopic( new TopicVO( tcm.getTopic() ) ) ;
        }
        
        this.problems.add( ptm ) ;
    }
}
