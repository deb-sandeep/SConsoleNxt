package com.sandy.sconsole.endpoints.rest.master.tag.vo;

import com.sandy.sconsole.dao.master.Tag;
import lombok.Data;

import java.time.Instant;

@Data
public class TagVO {

    private int     id ;
    private String  tagText ;
    private String  normalizedTagText ;
    private String  color ;
    private int     topicId ;
    private String  topicName ;
    private Instant createdAt ;

    // Populated only by the "get tags for topic" endpoint; -1 everywhere else.
    private int associationCount = -1 ;

    public TagVO(){}

    public TagVO( Tag t ) {
        this.id = t.getId() ;
        this.tagText = t.getTagText() ;
        this.normalizedTagText = t.getNormalizedTagText() ;
        this.color = t.getColor() ;
        this.topicId = t.getTopic().getId() ;
        this.topicName = t.getTopic().getTopicName() ;
        this.createdAt = t.getCreatedAt() ;
    }
}
