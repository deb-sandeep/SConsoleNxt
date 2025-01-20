package com.sandy.sconsole.api.master.dto;

import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.repo.BookRepo;
import lombok.Data;

import java.util.*;

@Data
public class BookProblemSummary {
    
    private int id ;
    private String subjectName ;
    private String seriesName ;
    private String bookName ;
    private String author ;
    private String bookShortName ;
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
        this.id = book.getId() ;
        this.subjectName = book.getSubject().getSubjectName() ;
        this.seriesName = book.getSeriesName() ;
        this.bookName = book.getBookName() ;
        this.author = book.getAuthor() ;
        this.bookShortName = book.getBookShortName() ;
    }
}
