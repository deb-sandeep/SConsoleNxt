package com.sandy.sconsole.api.master.vo;

import com.sandy.sconsole.dao.master.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChapterProblemsTopicMappingVO {
    
    @Data
    public static class ExerciseProblems {
        private int exerciseNum ;
        private String exerciseName ;
        private List<ProblemTopicMapping> problems = new ArrayList<>() ;
        
        ExerciseProblems( Problem problem, TopicChapterProblemMap tcpm ) {
            this.exerciseNum = problem.getExerciseNum() ;
            this.exerciseName = problem.getExerciseName() ;
            this.problems.add( new ProblemTopicMapping( problem, tcpm ) ) ;
        }
    }
    
    @Data
    public static class ProblemTopicMapping {
        private int problemId ;
        private String problemType ;
        private String problemKey ;
        private int mappingId = -1 ;
        private TopicVO topic = null ;
        
        ProblemTopicMapping( Problem problem, TopicChapterProblemMap tcpm ) {
            this.problemId = problem.getId() ;
            this.problemType = problem.getProblemType().getProblemType() ;
            this.problemKey = problem.getProblemKey() ;
            
            if( tcpm != null ) {
                this.mappingId = tcpm.getTopicChapterMap().getId() ;
                this.topic = new TopicVO( tcpm.getTopicChapterMap().getTopic() ) ;
            }
        }
    }
    
    private int chapterNum ;
    private String chapterName ;
    private BookVO book ;
    private List<ExerciseProblems> exercises = new ArrayList<>();
    
    public ChapterProblemsTopicMappingVO( Chapter chapter, Syllabus syllabus ) {
        this.book = new BookVO( chapter.getBook() ) ;
        this.book.setSyllabusName( syllabus.getSyllabusName() ) ;
        this.chapterNum = chapter.getId().getChapterNum() ;
        this.chapterName = chapter.getChapterName() ;
    }
    
    public void addProblemMapping( Problem problem, TopicChapterProblemMap tcpm ) {
        
        ProblemTopicMapping ptm = new ProblemTopicMapping( problem, tcpm ) ;
        
        if( exercises.isEmpty() ) {
            exercises.add( new ExerciseProblems( problem, tcpm ) );
        }
        else {
            ExerciseProblems lastEps = exercises.get( exercises.size()-1 ) ;
            if( lastEps.getExerciseNum() == problem.getExerciseNum() ) {
                lastEps.getProblems().add( ptm ) ;
            }
            else {
                exercises.add( new ExerciseProblems( problem, tcpm ) ) ;
            }
        }
    }
}
