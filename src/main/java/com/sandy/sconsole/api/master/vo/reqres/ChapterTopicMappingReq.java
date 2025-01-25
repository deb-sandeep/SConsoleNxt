package com.sandy.sconsole.api.master.vo.reqres;

import lombok.Data;

@Data
public class ChapterTopicMappingReq {
    
    private int mappingId = -1;
    private int bookId ;
    private int chapterNum ;
    private int topicId ;
}
