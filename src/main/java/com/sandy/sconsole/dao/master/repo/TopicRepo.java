package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Topic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicRepo extends CrudRepository<Topic, Integer> {
    
    interface TopicProblemTypeCount {
        int getTopicId() ;
        String getProblemType() ;
        int getNumProblems() ;
    }
    
    @Query( """
        select t
        from Topic  t
        where
            t.syllabus.syllabusName = :syllabusName
        order by
            t.id asc
    """)
    List<Topic> findTopics( @Param( "syllabusName" ) String syllabusName ) ;
    
    @Query( nativeQuery = true, value = """
        select
            t.id as topicId,
            p.problem_type as problemType,
            count( p.id ) as numProblems
        from topic_master t
            left outer join topic_chapter_map tcm
                on t.id = tcm.topic_id
            left outer join topic_chapter_problem_map tcpm
                on tcm.id = tcpm.topic_chapter_map_id
            left outer join problem_master p
                on p.id = tcpm.problem_id
        group by
            t.id, p.problem_type
        order by
            t.id
    """ )
    List<TopicProblemTypeCount> getTopicProblemCounts() ;
    
}
