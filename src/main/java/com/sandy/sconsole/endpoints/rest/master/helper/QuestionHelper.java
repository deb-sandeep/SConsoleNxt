package com.sandy.sconsole.endpoints.rest.master.helper;

import com.rometools.rome.io.impl.Base64;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.dao.master.repo.ProblemTypeRepo;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.test.Question;
import com.sandy.sconsole.dao.test.QuestionImage;
import com.sandy.sconsole.dao.test.repo.QuestionImageRepo;
import com.sandy.sconsole.dao.test.repo.QuestionRepo;
import com.sandy.sconsole.endpoints.rest.master.vo.QuestionImageVO;
import com.sandy.sconsole.endpoints.rest.master.vo.QuestionVO;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.QuestionRepoStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@Scope( "prototype" )
public class QuestionHelper {
    
    public static final String STATE_UNASSIGNED = "unassigned" ;
    public static final String STATE_ASSIGNED = "assigned" ;
    public static final String STATE_ATTEMPTED = "attempted" ;
    public static final String STATE_BURIED = "buried" ;
    
    @Autowired
    private QuestionRepo qRepo;
    
    @Autowired
    private QuestionImageRepo qImgRepo;
    
    @Autowired
    private TopicRepo topicRepo ;
    
    @Autowired
    private SyllabusRepo syllabusRepo ;
    
    @Autowired
    private SConsoleConfig config ;
    
    @Autowired
    private ProblemTypeRepo ptRepo ;
    
    public int saveQuestion( QuestionVO questionVO ) throws Exception {
        
        Question question = qRepo.findByQuestionId( questionVO.getQuestionId() ) ;
        
        if( question != null ) {
            log.debug( "  Existing question found. Deleting existing images." ) ;
            deleteQuestionImages( question ) ;
            qImgRepo.deleteAll( question.getQuestionImages() );
        }
        else {
            log.debug( "  No existing question found. Creating new question." ) ;
            question = new Question();
            question.setState( STATE_UNASSIGNED ) ;
            question.setRating( 0 ) ;
        }

        log.debug( "  Saving image files." ) ;
        saveImageFiles( questionVO ) ;
        
        log.debug( "  Saving question data." ) ;
        populateQuestionEntity( question, questionVO );
        question = qRepo.save( question );
        
        log.debug( "  Saving question images data." ) ;
        for( QuestionImageVO imgVO : questionVO.getQuestionImages() ) {
            QuestionImage qImg = new QuestionImage() ;
            populateQuestionImgEntity( qImg, question, imgVO ) ;
            qImgRepo.save( qImg ) ;
        }
        
        return question.getId() ;
    }
    
    private void deleteQuestionImages( Question question ) {
        for( QuestionImage qImg : question.getQuestionImages() ) {
            File imgFile = new File( config.getQuestionImgsFolder(), question.getSourceId() + "/" + qImg.getFileName() ) ;
            if( imgFile.exists() ) {
                FileUtils.deleteQuietly( imgFile ) ;
            }
        }
    }
    
    private void populateQuestionEntity( Question entity, QuestionVO question ) {
        
        entity.setQuestionId( question.getQuestionId() ) ;
        entity.setSyllabus( syllabusRepo.findById( question.getSyllabusName() ).get() ) ;
        entity.setTopic( topicRepo.findById( question.getTopicId() ).get() ) ;
        entity.setSourceId( question.getSourceId() ) ;
        entity.setProblemType( ptRepo.findById( question.getProblemType() ).get() ) ;
        entity.setLctSequence( question.getLctSequence() ) ;
        entity.setQuestionNumber( question.getQuestionNumber() ) ;
        entity.setAnswer( question.getAnswer() ) ;
        entity.setServerSyncTime( Instant.now() ) ;
    }
    
    private void populateQuestionImgEntity( QuestionImage entity, Question question, QuestionImageVO imgVO ) {
    
        entity.setQuestion( question ) ;
        entity.setSequence( imgVO.getSequence() ) ;
        entity.setPageNumber( imgVO.getPageNumber() ) ;
        entity.setFileName( imgVO.getFileName() ) ;
        entity.setLctCtxImage( imgVO.getLctCtxImage() ) ;
        entity.setPartNumber( imgVO.getPartNumber() ) ;
        entity.setImgWidth( imgVO.getImgWidth() ) ;
        entity.setImgHeight( imgVO.getImgHeight() ) ;
    }
    
    private void saveImageFiles( QuestionVO question ) throws Exception {
        
        for( QuestionImageVO imgVO : question.getQuestionImages() ) {
            
            log.debug( "    Saving image: {}", imgVO.getFileName() ) ;
            String srcPath = question.getSourceId() ;
            String fileName = imgVO.getFileName() ;
            File imgFile = new File( config.getQuestionImgsFolder(), srcPath + "/" + fileName ) ;
            
            byte[] imgData = Base64.decode( imgVO.getImgData().getBytes() ) ;
            FileUtils.writeByteArrayToFile( imgFile, imgData ) ;
        }
    }
    
    public QuestionRepoStatus getRepositoryStatus() {
        QuestionRepoStatus status = new QuestionRepoStatus() ;
        List<QuestionRepo.RepoStatusRow> statusRows = qRepo.getRepoStatus() ;
        for( QuestionRepo.RepoStatusRow s : statusRows ) {
            status.build( s ) ;
        }
        return status ;
    }
}
