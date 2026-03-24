package com.sandy.sconsole.endpoints.rest.master.exam.vo;

import com.sandy.sconsole.dao.exam.Question;
import com.sandy.sconsole.dao.exam.QuestionImage;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class QuestionVO {
    
    private Integer id;
    private String  questionId;
    private String  syllabusName;
    private String  topicName;
    private Integer topicId;
    private String  sourceId;
    private String  problemType;
    private Integer lctSequence;
    private Integer questionNumber;
    private String  answer;
    private Date    serverSyncTime;
    private List<QuestionImageVO> questionImages = new ArrayList<>() ;
    
    public QuestionVO(){}
    
    public QuestionVO( Question entity ) {
        
        this.setId( entity.getId() ) ;
        this.setQuestionId( entity.getQuestionId() ) ;
        this.setSyllabusName( entity.getSyllabus().getSyllabusName() ) ;
        this.setTopicId( entity.getTopic().getId() ) ;
        this.setTopicName( entity.getTopic().getTopicName() );
        this.setSourceId( entity.getSourceId() ) ;
        this.setProblemType( entity.getProblemType().getProblemType() ) ;
        this.setLctSequence( entity.getLctSequence() ) ;
        this.setQuestionNumber( entity.getQuestionNumber() ) ;
        this.setAnswer( entity.getAnswer() ) ;
        this.setServerSyncTime( Date.from( entity.getServerSyncTime() ) ) ;
        
        for( QuestionImage img : entity.getQuestionImages() ) {
            QuestionImageVO qImgVO = new QuestionImageVO() ;
            qImgVO.setSequence( img.getSequence() ) ;
            qImgVO.setPageNumber( img.getPageNumber() ) ;
            qImgVO.setFileName( img.getFileName() ) ;
            qImgVO.setLctCtxImage( img.getLctCtxImage() ) ;
            qImgVO.setPartNumber( img.getPartNumber() ) ;
            qImgVO.setImgWidth( img.getImgWidth() ) ;
            qImgVO.setImgHeight( img.getImgHeight() ) ;
            
            this.getQuestionImages().add( qImgVO ) ;
        }
    }
}
