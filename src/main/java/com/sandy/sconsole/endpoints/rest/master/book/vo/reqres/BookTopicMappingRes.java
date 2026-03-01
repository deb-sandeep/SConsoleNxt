package com.sandy.sconsole.endpoints.rest.master.book.vo.reqres;

import com.sandy.sconsole.endpoints.rest.master.book.vo.BookTopicMappingVO;
import com.sandy.sconsole.endpoints.rest.master.core.vo.TopicVO;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class BookTopicMappingRes {
    
    private String syllabusName ;
    private Map<Integer, TopicVO>    topicMap = new LinkedHashMap<>() ;
    private List<BookTopicMappingVO> bookTopicMappingList;
    
    public void setTopics( List<TopicVO> topics ) {
        topics.forEach( t -> topicMap.put( t.getId(), t ) ) ;
    }
}
