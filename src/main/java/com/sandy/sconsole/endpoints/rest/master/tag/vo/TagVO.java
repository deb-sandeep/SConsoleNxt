package com.sandy.sconsole.endpoints.rest.master.tag.vo;

import com.sandy.sconsole.dao.master.TagMaster;
import lombok.Data;

import java.time.Instant;

@Data
public class TagVO {

    private int     id ;
    private String  tagText ;
    private String  normalizedTagText ;
    private int     topicId ;
    private String  topicName ;
    private Instant createdAt ;

    public TagVO(){}

    public TagVO( TagMaster t ) {
        this.id = t.getId() ;
        this.tagText = t.getTagText() ;
        this.normalizedTagText = t.getNormalizedTagText() ;
        this.topicId = t.getTopic().getId() ;
        this.topicName = t.getTopic().getTopicName() ;
        this.createdAt = t.getCreatedAt() ;
    }
}
