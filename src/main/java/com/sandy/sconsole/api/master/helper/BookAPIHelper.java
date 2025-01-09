package com.sandy.sconsole.api.master.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.repo.BookRepo;
import com.sandy.sconsole.dao.master.repo.SubjectRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.sandy.sconsole.api.master.helper.BookMeta.*;

@Slf4j
@Component
public class BookAPIHelper {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyyMMddHHmmss" ) ;
    
    @Autowired SConsoleConfig config ;
    @Autowired SubjectRepo subjectRepo = null ;
    @Autowired BookRepo bookRepo = null ;
    
    private File uploadDir = null ;
    
    private File getUploadFolder() {
        if( uploadDir == null ) {
            uploadDir = new File( config.getWorkspacePath(), "uploads" ) ;
            if( !uploadDir.exists() ) {
                if( !uploadDir.mkdirs() ) {
                    log.error( "Uploads directory could not be created at " + uploadDir.getAbsolutePath() );
                }
            }
        }
        return uploadDir ;
    }
    
    public File saveUploadedFile( MultipartFile mFile ) throws Exception {
        
        String destFileName = mFile.getOriginalFilename() + "." + SDF.format( new Date() );
        File destFile = new File( getUploadFolder(), destFileName ) ;
        mFile.transferTo( destFile ) ;
        return destFile ;
    }
    
    public BookMeta parseBookMeta( File metaFile ) throws Exception {
        
        ObjectMapper mapper = new ObjectMapper( new YAMLFactory() ) ;
        mapper.findAndRegisterModules() ;
        
        return mapper.readValue( metaFile, BookMeta.class );
    }
    
    public void validateBookMeta( BookMeta meta ) {
        
        validateMandatoryFields( meta ) ;
        validateSubjectExists( meta.getSubject(), meta.getValidationMsgs() ) ;
        boolean isNewBook = validateBookExists( meta ) ;
        if( !isNewBook ) {
            validateChapterDiscrepancy( meta.getChapters(), meta.getValidationMsgs() ) ;
        }
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
    
    private void validateSubjectExists( String subject, List<ValidationMsg> msgs ) {
        
        boolean absent = subjectRepo.findById( subject ).isEmpty() ;
        if( absent ) {
            msgs.add( errMsg( "subject", subject, "Subject not registered" ) ) ;
        }
    }
    
    private boolean validateBookExists( BookMeta meta ) {
        
        String bookId = meta.getSubject() + " > " + meta.getName() + " by " + meta.getAuthor() ;
        Book book = bookRepo.findBook( meta.getSubject(), meta.getName(), meta.getAuthor() ) ;
        if( book == null ) {
            meta.getValidationMsgs().add( infoMsg( "name", bookId, "New book will be registered" ) ) ;
        }
        else {
            meta.getValidationMsgs().add( warnMsg( "name", bookId, "Book exists and will be overwritten" ) ) ;
        }
        return book == null ;
    }
    
    private void validateChapterDiscrepancy( List<ChapterMeta> chapters,
                                             List<ValidationMsg> msgs ) {
        
            
    }
}
