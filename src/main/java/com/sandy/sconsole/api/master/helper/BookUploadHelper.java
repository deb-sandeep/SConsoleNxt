package com.sandy.sconsole.api.master.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sandy.sconsole.api.master.vo.BookMeta;
import com.sandy.sconsole.api.master.vo.reqres.SaveBookMetaRes;
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
public class BookUploadHelper {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyyMMddHHmmss" ) ;
    
    public static class InvalidMetaFileException extends Exception {
        InvalidMetaFileException( String msg, Exception cause ) {
            super( msg, cause ) ;
        }
    }
    
    @Autowired SConsoleConfig config ;
    @Autowired BookMetaValidator validator ;
    
    @Autowired SubjectRepo subjectRepo ;
    @Autowired SyllabusRepo syllabusRepo ;
    @Autowired BookRepo bookRepo ;
    @Autowired ChapterRepo chapterRepo ;
    @Autowired ProblemRepo problemRepo ;
    @Autowired ProblemTypeRepo problemTypeRepo ;
    @Autowired SyllabusBookMapRepo syllabusBookMapRepo ;
    
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
    
    public BookMeta parseAndValidateBookMeta( File metaFile )
            throws InvalidMetaFileException {
        
        BookMeta meta ;
        try {
            ObjectMapper mapper = new ObjectMapper( new YAMLFactory() ) ;
            mapper.findAndRegisterModules() ;
            meta = mapper.readValue( metaFile, BookMeta.class );
            validator.validateBookMeta( meta ) ;
        }
        catch( Exception e ) {
            throw new InvalidMetaFileException( "Meta file is invalid. Check syntax.", e ) ;
        }
        return meta ;
    }
    
    @Transactional
    public SaveBookMetaRes saveBookMeta( BookMeta meta ) {
        
        SaveBookMetaRes stats = new SaveBookMetaRes() ;
        
        Book book = new Book() ;
        book.setSubjectName( meta.getSubject() ) ;
        book.setSeriesName( meta.getSeries() ) ;
        book.setBookName( meta.getName() ) ;
        book.setAuthor( meta.getAuthor() ) ;
        book.setBookShortName( meta.getShortName() ) ;
        book = bookRepo.save( book ) ;
        
        addChaptersToBook( book, meta.getChapters(), stats ) ;
        addBookSyllabusMapping( book ) ;

        return stats ;
    }
    
    private void addBookSyllabusMapping( Book book ) {
    
        List<Syllabus> syllabuses = syllabusRepo.findBySubjectName( book.getSubjectName() ) ;
        if( syllabuses != null && syllabuses.size() == 1 ) {
            SyllabusBookMap map = new SyllabusBookMap() ;
            map.setSyllabus( syllabuses.get( 0 ) ) ;
            map.setBook( book ) ;
            
            syllabusBookMapRepo.save( map ) ;
        }
    }
    
    private void addChaptersToBook( Book book,
                                    List<BookMeta.ChapterMeta> chMetas,
                                    SaveBookMetaRes stats ) {

        for( BookMeta.ChapterMeta chMeta : chMetas ) {
            
            ChapterId chapterId = new ChapterId( book.getId(), chMeta.getChapterNum() ) ;
            
            Chapter chapter = new Chapter() ;
            chapter.setId( chapterId );
            chapter.setBook( book ) ;
            chapter.setChapterName( chMeta.getChapterName() ) ;
            
            chapter = chapterRepo.save( chapter ) ;
            stats.incChaptersCreated() ;
            
            for( int i=0; i<chMeta.getExercises().size(); i++ ) {
                BookMeta.ExerciseMeta exMeta = chMeta.getExercises().get( i ) ;
                stats.incExercisesCreated() ;
                addProblemsToChapter( chapter, (i+1), exMeta, stats ) ;
            }
        }
    }
    
    private void addProblemsToChapter( Chapter chapter,
                                       int exerciseNum,
                                       BookMeta.ExerciseMeta exMeta,
                                       SaveBookMetaRes stats ) {
        
        String exName = exMeta.getName() ;
        for( BookMeta.ProblemCluster cluster : exMeta.getProblemClusters() ) {
            
            ProblemType type = problemTypeRepo.findById( cluster.getType() ).get() ;
            
            for( int i=cluster.getStartIndex(); i<=cluster.getEndIndex(); i++ ) {
                
                String problemKey = exName + "/" + type.getProblemType() ;
                if( cluster.getLctSequence() != null ) {
                    problemKey += "-" + cluster.getLctSequence() ;
                }
                problemKey += "/" + i ;
                
                Problem problem = new Problem() ;
                problem.setChapter( chapter ) ;
                problem.setExerciseNum( exerciseNum ) ;
                problem.setExerciseName( exName ) ;
                problem.setProblemType( type ) ;
                problem.setProblemKey( problemKey ) ;
                
                problemRepo.save( problem ) ;
                stats.incProblemsCreated() ;
            }
        }
    }
}
