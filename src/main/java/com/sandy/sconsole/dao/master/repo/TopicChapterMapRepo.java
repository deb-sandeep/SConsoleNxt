package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.TopicChapterMap;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicChapterMapRepo extends CrudRepository<TopicChapterMap, Integer> {
    
    List<TopicChapterMap> findByTopicOrderByChapter_Book_IdAsc( Topic topic );
    
    interface CTM {
        Chapter getChapter() ;
        TopicChapterMap getTopicChapterMap() ;
    }
    
    @Query( """
        select max(tcm.attemptSeq)+1
        from TopicChapterMap tcm
        where
            tcm.topic.id = :topicId
    """)
    Integer getNextAttemptSequence( @Param( "topicId" ) int topicId ) ;
    
    @Query( """
        select c as chapter,
               tcm as topicChapterMap
        from Chapter c
            left outer join TopicChapterMap tcm
                on tcm.chapter = c
        where
            c.book.id in :bookIds
        order by
            c.book.id asc,
            c.id.chapterNum asc,
            tcm.topic.id asc
    """ )
    List<Object[]> getTopicMappingsForBooks( @Param( "bookIds" ) Integer[] ids ) ;
    
    @Query( """
        select tcm
        from TopicChapterMap tcm
        where
            tcm.topic.syllabus.syllabusName = :syllabusName
        order by
            tcm.topic.id asc,
            tcm.attemptSeq asc
    """)
    List<TopicChapterMap> getTopicChapterMappingsForSyllabus( @Param( "syllabusName" ) String syllabusName ) ;
    
    @Query( """
        select tcm
        from TopicChapterMap tcm
        order by
            tcm.topic.id asc,
            tcm.attemptSeq asc
    """)
    List<TopicChapterMap> getAllTopicChapterMappings() ;
}
