package com.sandy.sconsole.endpoints.rest.master.core.vo;

import com.sandy.sconsole.dao.master.Topic;
import lombok.Data;

@Data
public class TopicVO {
    
    private int    id;
    private String syllabusName ;
    private String sectionName ;
    private String topicName ;
    
    public TopicVO(){}
    
    public TopicVO( Topic t ) {
        this.id = t.getId() ;
        this.syllabusName = t.getSyllabus().getSyllabusName() ;
        this.sectionName = t.getSectionName() ;
        this.topicName = t.getTopicName() ;
    }
}
