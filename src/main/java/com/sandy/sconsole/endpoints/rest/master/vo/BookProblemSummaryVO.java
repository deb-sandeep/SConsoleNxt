package com.sandy.sconsole.endpoints.rest.master.vo;

import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.dto.BookDTO;
import com.sandy.sconsole.dao.master.repo.BookRepo;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class BookProblemSummaryVO {

    private BookDTO book = new BookDTO() ;
    private List<ChapterProblemSummary> chapterProblemSummaries = new ArrayList<>() ;

    @Data
    public static class ChapterProblemSummary {
        
        private int chapterNum ;
        private String chapterName ;
        private List<ExerciseProblemSummary> exerciseProblemSummaries = new ArrayList<>() ;
        
        public ChapterProblemSummary( BookRepo.ProblemTypeCount ptc ) {
            this.chapterNum = ptc.getChapterNum() ;
            this.chapterName = ptc.getChapterName() ;
        }
    }
    
    @Data
    public static class ExerciseProblemSummary {
        
        private int exerciseNum ;
        private String exerciseName ;
        private Map<String, Integer> problemTypeCount = new LinkedHashMap<>() ;
        
        public ExerciseProblemSummary( BookRepo.ProblemTypeCount ptc ) {
            this.exerciseNum = ptc.getExerciseNum() ;
            this.exerciseName = ptc.getExerciseName() ;
            
            updateProblemTypeCount( ptc ) ;
        }
        
        public void updateProblemTypeCount( BookRepo.ProblemTypeCount ptc ) {
            problemTypeCount.put( ptc.getProblemType(),
                                  ptc.getNumProblems() ) ;
        }
    }
    
    public BookProblemSummaryVO( Book book ) {
        this.getBook().setId( book.getId() ) ;
        this.getBook().setSubjectName( book.getSubjectName() ) ;
        this.getBook().setSeriesName( book.getSeriesName() ) ;
        this.getBook().setBookName( book.getBookName() ) ;
        this.getBook().setAuthor( book.getAuthor() ) ;
        this.getBook().setBookShortName( book.getBookShortName() ) ;
        this.getBook().setExtensible( book.isExtensible() ) ;
        this.getBook().setTopicMappingDone( book.isTopicMappingDone() ) ;
    }
}
