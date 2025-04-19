package com.sandy.sconsole.endpoints.rest.master.helper;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.repo.*;
import com.sandy.sconsole.endpoints.rest.master.vo.BookProblemSummaryVO;
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
    
    @Autowired private SConsoleConfig config ;
    @Autowired private BookMetaValidator validator ;
    
    @Autowired private SyllabusRepo syllabusRepo ;
    @Autowired private BookRepo bookRepo ;
    @Autowired private ChapterRepo chapterRepo ;
    @Autowired private ProblemRepo problemRepo ;
    @Autowired private ProblemTypeRepo problemTypeRepo ;
    @Autowired private SyllabusBookMapRepo syllabusBookMapRepo ;
    
    public List<BookRepo.BookSummary> getAllBookSummaries() {
        return bookRepo.findAllBooks() ;
    }
    
    public BookProblemSummaryVO getBookProblemsSummary( int bookId ) {
        
        Book book = bookRepo.findById( bookId ).get() ;
        Syllabus syllabus = syllabusBookMapRepo.findByBook( book ) ;
        
        BookProblemSummaryVO bps = new BookProblemSummaryVO( book ) ;
        bps.getBook().setSyllabusName( syllabus.getSyllabusName() ) ;
        
        List<BookRepo.ProblemTypeCount> problemTypeCounts ;
        problemTypeCounts = bookRepo.getProblemSummariesForChapter( bookId ) ;
        
        int lastChNum = -1 ;
        int lastExNum = -1 ;
        BookProblemSummaryVO.ChapterProblemSummary  cps = null ;
        BookProblemSummaryVO.ExerciseProblemSummary eps = null ;
        
        for( BookRepo.ProblemTypeCount ptc : problemTypeCounts ) {
            if( ptc.getChapterNum() != lastChNum ) {
                cps = new BookProblemSummaryVO.ChapterProblemSummary( ptc ) ;
                bps.getChapterProblemSummaries().add( cps ) ;
                
                if( ptc.getExerciseNum() != null ) {
                    // If we have a chapter with no exercises, skip creating an
                    // exercise problem summary instance. Case: When a new chapter
                    // is being dynamically added via UI
                    eps = new BookProblemSummaryVO.ExerciseProblemSummary( ptc ) ;
                    cps.getExerciseProblemSummaries().add( eps ) ;
                    lastExNum = ptc.getExerciseNum() ;
                }
                lastChNum = ptc.getChapterNum() ;
            }
            else if( ptc.getExerciseNum() != lastExNum ) {
                eps = new BookProblemSummaryVO.ExerciseProblemSummary( ptc ) ;
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
