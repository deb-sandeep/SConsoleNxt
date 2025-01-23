package com.sandy.sconsole.api.master.dto;

import com.sandy.sconsole.dao.master.Topic;
import lombok.Data;

@Data
public class TopicDTO {
    
    private int topicId ;
    private String syllabusName ;
    private String sectionName ;
    private String topicName ;
    
    public TopicDTO( Topic t ) {
        this.topicId = t.getId() ;
        this.syllabusName = t.getSyllabus().getSyllabusName() ;
        this.sectionName = t.getSectionName() ;
        this.topicName = t.getTopicName() ;
    }
}
