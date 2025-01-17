package com.sandy.sconsole.api.master.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sandy.sconsole.api.master.dto.BookMeta;
import com.sandy.sconsole.api.master.dto.SaveBookMetaRes;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.master.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class BookAPIHelper {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyyMMddHHmmss" ) ;
    
    @Autowired SConsoleConfig config ;
    @Autowired BookMetaValidator validator ;
    
    @Autowired SubjectRepo subjectRepo ;
    @Autowired BookRepo bookRepo ;
    @Autowired ChapterRepo chapterRepo ;
    @Autowired ProblemRepo problemRepo ;
    @Autowired ProblemTypeRepo problemTypeRepo ;
    
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
    
    public File getUploadedFile( String fileName ) {
        return new File( getUploadFolder(), fileName ) ;
    }
    
    public BookMeta parseAndValidateBookMeta( File metaFile ) throws Exception {
        
        ObjectMapper mapper = new ObjectMapper( new YAMLFactory() ) ;
        mapper.findAndRegisterModules() ;
        
        BookMeta meta = mapper.readValue( metaFile, BookMeta.class );
        validator.validateBookMeta( meta ) ;
        
        return meta ;
    }
    
    @Transactional
    public SaveBookMetaRes saveBookMeta( BookMeta meta ) {
        
        SaveBookMetaRes stats   = new SaveBookMetaRes() ;
        Subject         subject = subjectRepo.findById( meta.getSubject() ).get() ;
        
        Book book = new Book() ;
        book.setSubject( subject ) ;
        book.setSeriesName( meta.getSeries() ) ;
        book.setBookName( meta.getName() ) ;
        book.setAuthor( meta.getAuthor() ) ;
        book.setBookShortName( meta.getShortName() ) ;
        book = bookRepo.save( book ) ;
        
        addChaptersToBook( book, meta.getChapters(), stats ) ;

        return stats ;
    }
    
    private void addChaptersToBook( Book book,
                                    List<BookMeta.ChapterMeta> chMetas,
                                    SaveBookMetaRes stats ) {

        for( BookMeta.ChapterMeta chMeta : chMetas ) {
            
            ChapterId chapterId = new ChapterId() ;
            chapterId.setBookId( book.getId() ) ;
            chapterId.setChapterNum( chMeta.getChapterNum() ) ;
            
            Chapter chapter = new Chapter() ;
            chapter.setId( chapterId );
            chapter.setBook( book ) ;
            chapter.setChapterName( chMeta.getTitle() ) ;
            
            chapter = chapterRepo.save( chapter ) ;
            stats.incChaptersCreated() ;
            
            for( BookMeta.ExerciseMeta exMeta : chMeta.getExercises() ) {
                stats.incExercisesCreated() ;
                addProblemsToChapter( chapter, exMeta, stats ) ;
            }
        }
    }
    
    private void addProblemsToChapter( Chapter chapter,
                                       BookMeta.ExerciseMeta exMeta,
                                       SaveBookMetaRes stats ) {
        
        String exName = exMeta.getName() ;
        
        for( BookMeta.ProblemCluster cluster : exMeta.getProblemClusters() ) {
            
            ProblemType type = problemTypeRepo.findById( cluster.getType() ).get() ;
            
            for( int i=cluster.getStartIndex(); i<=cluster.getEndIndex(); i++ ) {
                
                String problemId = exName + "/" + type.getProblemType() ;
                if( cluster.getLctSequence() != null ) {
                    problemId += "-" + cluster.getLctSequence() ;
                }
                problemId += "/" + i ;
                
                Problem problem = new Problem() ;
                problem.setChapter( chapter ) ;
                problem.setExerciseName( exName ) ;
                problem.setProblemType( type ) ;
                problem.setProblemId( problemId ) ;
                
                problemRepo.save( problem ) ;
                stats.incProblemsCreated() ;
            }
        }
    }
}
