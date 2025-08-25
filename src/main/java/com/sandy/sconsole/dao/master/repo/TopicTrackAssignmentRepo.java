package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface TopicTrackAssignmentRepo extends JpaRepository<TopicTrackAssignment, Integer> {
    
    @Modifying( flushAutomatically = true, clearAutomatically = true )
    @Query( """
        delete from TopicTrackAssignment t where t.trackId = ?1
    """)
    void deleteByTrackId( Integer trackId ) ;
    
    @Modifying( flushAutomatically = true, clearAutomatically = true )
    @Query( """
        delete from TopicTrackAssignment t where t.topicId in ?1
    """)
    void deleteByTopicId( List<Integer> topicIds ) ;
    
    @Query( """
        select tta
        from TopicTrackAssignment tta
        where
            :date between tta.startDate and tta.endDate
        order by
            tta.topicId
    """)
    List<TopicTrackAssignment> findActiveAssignments( Date date ) ;
    
    TopicTrackAssignment findByTopicId( Integer topicId ) ;
    
    List<TopicTrackAssignment> findByTrackId( Integer trackId ) ;
}
