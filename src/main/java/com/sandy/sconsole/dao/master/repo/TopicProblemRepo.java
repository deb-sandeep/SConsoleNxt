package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TopicProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopicProblemRepo extends JpaRepository<TopicProblem, Integer> {

    @Query( """
    select tp
    from TopicProblem tp
    where tp.topicId = :topicId
    order by tp.problemId
    """)
    List<TopicProblem> findByTopicId( Integer topicId ) ;
    
    @Query( """
    select tp
    from TopicProblem tp
    where tp.topicId = :topicId and
          tp.problemState in ( 'Assigned', 'Later', 'Redo' )
    """)
    List<TopicProblem> findActiveProblems( Integer topicId ) ;
    
    @Query( """
    select tp
    from TopicProblem tp
    where tp.topicId = :topicId and
          tp.problemState = 'Pigeon'
    order by tp.problemId
    """)
    List<TopicProblem> findPigeonedProblems( Integer topicId ) ;
    
    @Query( """
    select count(tp)
    from TopicProblem tp
    where tp.topicId = :topicId and
          tp.problemState = 'Pigeon'
    order by tp.problemId
    """)
    Integer findNumPigeonedProblems( Integer topicId ) ;
}
