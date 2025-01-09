package com.sandy.sconsole.api.master.helper;

import com.sandy.sconsole.core.util.StringUtil;
import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.ProblemType;
import com.sandy.sconsole.dao.master.repo.BookRepo;
import com.sandy.sconsole.dao.master.repo.ProblemTypeRepo;
import com.sandy.sconsole.dao.master.repo.SubjectRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.sandy.sconsole.api.master.helper.BookMeta.*;
import static com.sandy.sconsole.api.master.helper.BookMeta.errMsg;

@Slf4j
@Component
public class BookMetaValidator {
    
    @Autowired SubjectRepo     subjectRepo     = null ;
    @Autowired BookRepo        bookRepo        = null ;
    @Autowired ProblemTypeRepo problemTypeRepo = null ;

    public void validateBookMeta( BookMeta meta ) {
        
        validateMandatoryFields( meta ) ;
        validateSubjectExists( meta.getSubject(), meta.getValidationMsgs() ) ;
        
        Book book = validateBookExists( meta ) ;
        if( book != null ) {
            validateChapterDiscrepancy( meta.getChapters(),
                    book.getChapters(),
                    meta.getValidationMsgs() ) ;
        }
        
        for( BookMeta.ChapterMeta chapterMeta : meta.getChapters() ) {
            validateChapterMetaValues( chapterMeta ) ;
        }
        
        meta.updateMsgCount() ;
    }
    
    private void validateMandatoryFields( BookMeta meta ) {
        
        if( meta.getSubject() == null ) {
            meta.getValidationMsgs().add( errMsg( "subject", "Subject is missing" ) ) ;
        }
        
        if( meta.getSeries() == null ) {
            meta.getValidationMsgs().add( warnMsg( "series", "Series is missing" ) ) ;
        }
        
        if( meta.getName() == null ) {
            meta.getValidationMsgs().add( errMsg( "name", "Name is missing" ) ) ;
        }
        
        if( meta.getAuthor() == null ) {
            meta.getValidationMsgs().add( errMsg( "author", "Author is missing" ) ) ;
        }
        
        if( meta.getShortName() == null ) {
            meta.getValidationMsgs().add( errMsg( "shortName", "Short name is missing" ) ) ;
        }
        
        for( BookMeta.ChapterMeta chapter : meta.getChapters() ) {
            
            if( chapter.getTitle() == null ) {
                chapter.getValidationMsgs().add( errMsg( "title", "Title is missing" ) ) ;
            }
            for( BookMeta.ExerciseMeta exercise : chapter.getExercises() ) {
                
                if( exercise.getName() == null ) {
                    exercise.getValidationMsgs().add( errMsg( "name", "Name is missing" ) ) ;
                }
            }
        }
    }
    
    private void validateSubjectExists( String subject, List<BookMeta.ValidationMsg> msgs ) {
        
        boolean absent = subjectRepo.findById( subject ).isEmpty() ;
        if( absent ) {
            msgs.add( errMsg( "subject", subject, "Subject not registered" ) ) ;
        }
    }
    
    private Book validateBookExists( BookMeta meta ) {
        
        String bookId = meta.getSubject() + " > " + meta.getName() + " by " + meta.getAuthor() ;
        Book book = bookRepo.findBook( meta.getSubject(), meta.getName(), meta.getAuthor() ) ;
        if( book == null ) {
            meta.getValidationMsgs().add( infoMsg( "name", bookId, "New book will be registered" ) ) ;
        }
        else {
            meta.getValidationMsgs().add( warnMsg( "name", bookId, "Book exists and will be overwritten" ) ) ;
        }
        return book ;
    }
    
    private void validateChapterDiscrepancy( List<ChapterMeta> chapters,
                                             Set<Chapter> existingChapters,
                                             List<ValidationMsg> msgs ) {
        
        log.error( "TODO: This function needs implementation after a new book save." );
    }
    
    private void validateChapterMetaValues( ChapterMeta chapterMeta ) {
        
        // A chapter title is of the format "<chaterNum:num> - <chapterTitle>"
        String[] titleParts = chapterMeta.getTitle().split( "-" ) ;
        if( titleParts.length < 2 ) {
            chapterMeta.getValidationMsgs().add( errMsg( "title", "Title format is incorrect" ) ) ;
        }
        else {
            try {
                Integer.parseInt( titleParts[0].trim() ) ;
            }
            catch( Exception e ) {
                chapterMeta.getValidationMsgs()
                        .add( errMsg( "title", "Chapter number is not a number" ) ) ;
            }
            
            if( StringUtil.isEmptyOrNull( titleParts[1] ) ) {
                chapterMeta.getValidationMsgs()
                        .add( errMsg( "title", "Chapter title is missing" ) ) ;
            }
        }
        
        for( ExerciseMeta exercise : chapterMeta.getExercises() ) {
            validateExerciseMetaValues( exercise ) ;
        }
    }
    
    private void validateExerciseMetaValues( ExerciseMeta exercise ) {
        
        if( StringUtil.isEmptyOrNull( exercise.getName() ) ) {
            exercise.getValidationMsgs().add( errMsg( "name", "Name is missing or empty" ) ) ;
        }
        
        if( exercise.getProblems().isEmpty() ) {
            exercise.getValidationMsgs().add( errMsg( "problems", "No problems defined" ) ) ;
        }
        
        for( String problemClusterMeta : exercise.getProblems() ) {
            ProblemCluster problemCluster ;
            problemCluster = parseAndValidateProblemCluster( problemClusterMeta,
                                                             exercise.getValidationMsgs() ) ;
            
            exercise.getProblemClusters().add( problemCluster ) ;
        }
    }
    
    // Problem cluster meta is of the format
    // <SUB|SCA|MCA|MMT|LCT|NVT> <LCT_SEQ>? <START_COUNT>-<END_COUNT>
    private ProblemCluster parseAndValidateProblemCluster( String data, List<ValidationMsg> msgs ) {
        
        ProblemCluster cluster = new ProblemCluster() ;
        String[] parts = data.split( " " ) ;
        
        if( parts.length < 2 ) {
            msgs.add( errMsg( "problems", data, "Invalid problem cluster format" ) ) ;
            return null ;
        }
        
        ProblemType type = problemTypeRepo.findById( parts[0].trim() ).orElse( null ) ;
        if( type == null ) {
            msgs.add( errMsg( "problems", data, "Invalid problem type - " + parts[0] ) ) ;
            return null ;
        }
        else {
            cluster.setType( type.getProblemType() ) ;
        }
        
        if( type.getProblemType().equals( "LCT" )  ) {
            if( parts.length != 3 ) {
                msgs.add( errMsg( "problems", data, "LCT problem type should have 3 parts" ) ) ;
                return null ;
            }
            else {
                cluster.setLctSequence( parts[1].trim() );
            }
        }
        
        String[] rangeParts = parts[parts.length-1].split( "-" ) ;
        cluster.setStartIndex( Integer.parseInt( rangeParts[0] ) ) ;
        if( rangeParts.length == 1 ) {
            msgs.add( warnMsg( "problems", data, "Cluster has only one problem" ) ) ;
            cluster.setEndIndex( cluster.getStartIndex() ) ;
        }
        else {
            cluster.setEndIndex( Integer.parseInt( rangeParts[1] ) ) ;
        }
        return cluster ;
    }
}
