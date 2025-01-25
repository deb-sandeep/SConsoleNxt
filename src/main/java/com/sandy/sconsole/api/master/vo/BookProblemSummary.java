package com.sandy.sconsole.api.master.vo;

import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.repo.BookRepo;
import lombok.Data;

import java.util.*;

@Data
public class BookProblemSummary {

    private BookVO                      book                    = new BookVO() ;
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
    
    public BookProblemSummary( Book book ) {
        this.getBook().setId( book.getId() ) ;
        this.getBook().setSubjectName( book.getSubject().getSubjectName() ) ;
        this.getBook().setSeriesName( book.getSeriesName() ) ;
        this.getBook().setBookName( book.getBookName() ) ;
        this.getBook().setAuthor( book.getAuthor() ) ;
        this.getBook().setBookShortName( book.getBookShortName() ) ;
    }
}
