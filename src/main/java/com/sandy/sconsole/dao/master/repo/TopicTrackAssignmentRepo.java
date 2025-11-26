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
    
    @Query( nativeQuery = true, value = """
        select *
        from topic_track_assignment tta
        where
            :date >= tta.start_date
            and :date < DATE_ADD(tta.end_date, INTERVAL 1 DAY)
        order by tta.topic_id
    """ )
    List<TopicTrackAssignment> findActiveAssignments( Date date ) ;
    
    @Query( """
        select tta
        from TopicTrackAssignment tta
            left outer join Track  t
                on tta.trackId = t.id
        where
            t.syllabusName = :syllabusName
        order by
            tta.startDate
    """)
    List<TopicTrackAssignment> findBySyllabus( String syllabusName ) ;
    
    TopicTrackAssignment findByTopicId( Integer topicId ) ;
    
    List<TopicTrackAssignment> findByTrackId( Integer trackId ) ;
}
