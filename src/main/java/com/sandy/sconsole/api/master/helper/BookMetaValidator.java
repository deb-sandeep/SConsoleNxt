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
        
        // 1 - Perform a structural validation (mandatory fields)
        validateMandatoryFields( meta ) ;
        
        // 2 - Check if subject exits. If not, there is no need to
        //     validate further. The entire file is defunct.
        boolean subjectExists = validateSubjectExists( meta.getSubject(),
                                                       meta.getValidationMsgs() ) ;
        
        if( subjectExists ) {
            // 3 - If subject exists, validate the chapter metadata values.
            //     This recursively validates the problem cluster metadata too.
            for( BookMeta.ChapterMeta chapterMeta : meta.getChapters() ) {
                validateChapterMetaValues( chapterMeta ) ;
            }

            // 4 - Check if the book exists. If it does, check if the chapter
            //     metadata has changed. If it has, then the differences need
            //     to be approved by the user.
            Book book = validateBookExists( meta ) ;
            if( book != null ) {
                validateChapterDiscrepancy( meta.getChapters(),
                                            book.getChapters(),
                                            meta.getValidationMsgs() ) ;
            }
        }
        
        // Finally update the consolidated message count.
        meta.updateMsgCount() ;
    }
    
    private void validateMandatoryFields( BookMeta meta ) {
        
        List<ValidationMsg> msgs = meta.getValidationMsgs() ;
        
        if( meta.getSubject() == null ) {
            msgs.add( errMsg( "subject", "Subject is missing" ) ) ;
        }
        
        if( meta.getSeries() == null ) {
            msgs.add( warnMsg( "series", "Series is missing" ) ) ;
        }
        
        if( meta.getName() == null ) {
            msgs.add( errMsg( "name", "Name is missing" ) ) ;
        }
        
        if( meta.getAuthor() == null ) {
            msgs.add( errMsg( "author", "Author is missing" ) ) ;
        }
        
        if( meta.getShortName() == null ) {
            msgs.add( errMsg( "shortName", "Short name is missing" ) ) ;
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
    
    private boolean validateSubjectExists( String subject, List<BookMeta.ValidationMsg> msgs ) {
        
        boolean present = subjectRepo.findById( subject ).isPresent();
        if( !present ) {
            msgs.add( errMsg( "subject", subject, "Subject not registered" ) ) ;
        }
        return present ;
    }
    
    private Book validateBookExists( BookMeta meta ) {
        
        List<ValidationMsg> msgs = meta.getValidationMsgs() ;
        String bookId = meta.getSubject() + " > " + meta.getName() + " by " + meta.getAuthor() ;
        Book book = bookRepo.findBook( meta.getSubject(), meta.getName(), meta.getAuthor() ) ;
        
        if( book == null ) {
            msgs.add( infoMsg( "name", bookId, "New book will be registered" ) ) ;
        }
        else {
            msgs.add( warnMsg( "name", bookId, "Book exists and will be overwritten" ) ) ;
        }
        return book ;
    }
    
    private void validateChapterDiscrepancy( List<ChapterMeta> chapters,
                                             Set<Chapter> existingChapters,
                                             List<ValidationMsg> msgs ) {
        
        log.error( "TODO: **** This function needs implementation after a new book save." );
    }
    
    private void validateChapterMetaValues( ChapterMeta meta ) {
        
        List<ValidationMsg> msgs = meta.getValidationMsgs() ;
        String[] titleParts = meta.getTitle().split( "-" ) ;
        
        if( titleParts.length < 2 ) {
            msgs.add( errMsg( "title", "Title format is incorrect" ) ) ;
        }
        else {
            try {
                Integer.parseInt( titleParts[0].trim() ) ;
            }
            catch( Exception e ) {
                msgs.add( errMsg( "title", "Chapter number is not a number" ) ) ;
            }
            
            if( StringUtil.isEmptyOrNull( titleParts[1] ) ) {
                msgs.add( errMsg( "title", "Chapter title is missing" ) ) ;
            }
        }
        
        for( ExerciseMeta exercise : meta.getExercises() ) {
            validateExerciseMetaValues( exercise ) ;
        }
    }
    
    private void validateExerciseMetaValues( ExerciseMeta exercise ) {
        
        List<ValidationMsg> msgs = exercise.getValidationMsgs() ;
        
        if( StringUtil.isEmptyOrNull( exercise.getName() ) ) {
            msgs.add( errMsg( "name", "Name is missing or empty" ) ) ;
        }
        
        if( exercise.getProblems().isEmpty() ) {
            msgs.add( errMsg( "problems", "No problems defined" ) ) ;
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
        
        ProblemCluster cluster = new ProblemCluster( data ) ;
        
        String[] parts = data.split( " " ) ;
        if( parts.length < 2 ) {
            msgs.add( errMsg( "problems", data, "Invalid problem cluster format" ) ) ;
            return cluster ;
        }
        
        ProblemType type = problemTypeRepo.findById( parts[0].trim() ).orElse( null ) ;
        if( type == null ) {
            msgs.add( errMsg( "problems", data, "Invalid problem type - " + parts[0] ) ) ;
            return cluster ;
        }
        else {
            cluster.setType( type.getProblemType() ) ;
        }
        
        if( type.getProblemType().equals( "LCT" )  ) {
            if( parts.length != 3 ) {
                msgs.add( errMsg( "problems", data, "LCT problem type should have 3 parts" ) ) ;
                return cluster ;
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
