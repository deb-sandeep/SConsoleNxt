package com.sandy.sconsole.api.master.helper;

import com.sandy.sconsole.api.master.vo.BookProblemSummary;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
@Component
public class BookHelper {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyyMMddHHmmss" ) ;
    
    public static class InvalidMetaFileException extends Exception {
        InvalidMetaFileException( String msg, Exception cause ) {
            super( msg, cause ) ;
        }
    }
    
    @Autowired SConsoleConfig config ;
    @Autowired BookMetaValidator validator ;
    
    @Autowired SyllabusRepo syllabusRepo ;
    @Autowired BookRepo bookRepo ;
    @Autowired ChapterRepo chapterRepo ;
    @Autowired ProblemRepo problemRepo ;
    @Autowired ProblemTypeRepo problemTypeRepo ;
    @Autowired SyllabusBookMapRepo syllabusBookMapRepo ;
    
    public List<BookRepo.BookSummary> getAllBookSummaries() {
        return bookRepo.findAllBooks() ;
    }
    
    public BookProblemSummary getBookProblemsSummary( int bookId ) {
        
        Book book = bookRepo.findById( bookId ).get() ;
        Syllabus syllabus = syllabusBookMapRepo.findByBook( book ) ;
        
        BookProblemSummary bps = new BookProblemSummary( book ) ;
        bps.getBook().setSyllabusName( syllabus.getSyllabusName() ) ;
        
        List<BookRepo.ProblemTypeCount> problemTypeCounts ;
        problemTypeCounts = bookRepo.getProblemSummariesForChapter( bookId ) ;
        
        int lastChNum = -1 ;
        int lastExNum = -1 ;
        BookProblemSummary.ChapterProblemSummary cps = null ;
        BookProblemSummary.ExerciseProblemSummary eps = null ;
        
        for( BookRepo.ProblemTypeCount ptc : problemTypeCounts ) {
            if( ptc.getChapterNum() != lastChNum ) {
                cps = new BookProblemSummary.ChapterProblemSummary( ptc ) ;
                eps = new BookProblemSummary.ExerciseProblemSummary( ptc ) ;
                
                cps.getExerciseProblemSummaries().add( eps ) ;
                bps.getChapterProblemSummaries().add( cps ) ;
                
                lastChNum = ptc.getChapterNum() ;
                lastExNum = ptc.getExerciseNum() ;
            }
            else if( ptc.getExerciseNum() != lastExNum ) {
                eps = new BookProblemSummary.ExerciseProblemSummary( ptc ) ;
                if( cps != null ) {
                    cps.getExerciseProblemSummaries().add( eps ) ;
                }
                else {
                    throw new IllegalStateException( "CPS is null. This should not happen." ) ;
                }
                lastExNum = ptc.getExerciseNum() ;
            }
            else {
                if( eps != null ) {
                    eps.updateProblemTypeCount( ptc ) ;
                }
                else {
                    throw new IllegalStateException( "EPS is null. This should not happen." ) ;
                }
            }
        }
        
        return bps ;
    }
    
    public int updateExerciseName( int bookId, int chapterNum,
                                   int exerciseNum, String exerciseName ) {
        
        List<Problem> problems = problemRepo.getProblems( bookId, chapterNum, exerciseNum ) ;
        problems.forEach( p -> {
            String oldProblemKey = p.getProblemKey() ;
            String newProblemKey = exerciseName + oldProblemKey.substring( oldProblemKey.indexOf( '/' ) ) ;
            
            p.setExerciseName( exerciseName ) ;
            p.setProblemKey( newProblemKey ) ;
        } ) ;
        problemRepo.saveAll( problems ) ;
        return problems.size() ;
    }
    
}
