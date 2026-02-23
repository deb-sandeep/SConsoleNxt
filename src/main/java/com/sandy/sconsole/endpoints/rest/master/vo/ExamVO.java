package com.sandy.sconsole.endpoints.rest.master.vo;

import com.sandy.sconsole.dao.exam.Exam;
import com.sandy.sconsole.dao.exam.ExamSection;
import com.sandy.sconsole.dao.exam.ExamTopic;
import com.sandy.sconsole.dao.master.dto.TopicVO;
import lombok.Data;

import java.util.*;

@Data
public class ExamVO {
    
    private Integer id ;
    private String state ;
    private String type ;
    private String note ;
    private Integer numPhyQuestions ;
    private Integer numChemQuestions ;
    private Integer numMathQuestions ;
    private Integer totalMarks ;
    private Integer duration ;
    private Date creationDate ;
    private List<ExamSectionVO> sections = new ArrayList<>() ;
    private Map<String, List<TopicVO>> topics = new HashMap<>() ;
    
    public ExamVO(){}
    
    public ExamVO( Exam entity ) {
        this( entity, true ) ;
    }

    public ExamVO( Exam entity, boolean deep ) {
        this.setId( entity.getId() ) ;
        this.setState( entity.getState() ) ;
        this.setType( entity.getType() ) ;
        this.setNote( entity.getNote() ) ;
        this.setNumPhyQuestions( entity.getNumPhyQuestions() ) ;
        this.setNumChemQuestions( entity.getNumChemQuestions() ) ;
        this.setNumMathQuestions( entity.getNumMathQuestions() ) ;
        this.setTotalMarks( entity.getTotalMarks() ) ;
        this.setDuration( entity.getDuration() ) ;
        this.setCreationDate( Date.from( entity.getCreationDate() ) ) ;

        if( deep ) {
            for( ExamSection section : entity.getSections() ) {
                this.getSections().add( new ExamSectionVO( section ) ) ;
            }
            
            for( ExamTopic t : entity.getTopics() ) {
                String syllabusName = t.getTopic().getSyllabus().getSyllabusName() ;
                List<TopicVO> topicList = topics.computeIfAbsent( syllabusName, k -> new ArrayList<>() ) ;
                topicList.add( new TopicVO( t.getTopic() ) ) ;
            }
        }
    }
}
