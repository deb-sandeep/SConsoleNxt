package com.sandy.sconsole.api.master.dto;

import lombok.Data;

@Data
public class ChapterTopicMappingReq {
    
    private int mappingId ;
    private int bookId ;
    private int chapterNum ;
    private int topicId ;
}
