package com.sandy.sconsole.api.master.helper;

import com.sandy.sconsole.api.master.dto.*;
import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.ChapterId;
import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.TopicChapterMap;
import com.sandy.sconsole.dao.master.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChapterTopicMappingHelper {
    
    @Autowired BookRepo bookRepo ;
    @Autowired ChapterRepo chapterRepo ;
    @Autowired TopicRepo topicRepo ;
    @Autowired TopicChapterMapRepo tcmRepo ;
    
    public void createOrUpdateMapping( ChapterTopicMappingReq req ) {
        
        TopicChapterMap map ;
        
        if( req.getMappingId() == -1 ) {
            map = new TopicChapterMap() ;
        }
        else {
            map = tcmRepo.findById( req.getMappingId() ).get() ;
        }
        
        ChapterId chapterId = new ChapterId( req.getBookId(), req.getChapterNum() ) ;
        Chapter ch = chapterRepo.findById( chapterId ).get() ;
        Topic topic = topicRepo.findById( req.getTopicId() ).get() ;
        
        map.setChapter( ch ) ;
        map.setTopic( topic ) ;
        
        tcmRepo.save( map ) ;
    }
    
    public void deleteMapping( Integer mapId ) {
        tcmRepo.deleteById( mapId ) ;
    }
}
