package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Topic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicRepo extends CrudRepository<Topic, Integer> {
    
    @Query( """
        select t
        from Topic  t
        where
            t.syllabus.syllabusName = :syllabusName
        order by
            t.id asc
    """)
    List<Topic> findTopics( @Param( "syllabusName" ) String syllabusName ) ;
}
