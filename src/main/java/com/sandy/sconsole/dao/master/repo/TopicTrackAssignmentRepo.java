package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TopicTrackAssignmentRepo extends CrudRepository<TopicTrackAssignment, Integer> {
    
    @Modifying
    @Query( """
        delete from TopicTrackAssignment t where t.trackId = ?1
    """)
    void deleteByTrackId( Integer trackId );
}
