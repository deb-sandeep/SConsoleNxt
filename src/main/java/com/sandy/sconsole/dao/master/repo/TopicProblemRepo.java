package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TopicProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopicProblemRepo extends JpaRepository<TopicProblem, Integer> {
    
    interface ProblemStateCount {
        String getState() ;
        int getNumProblems() ;
    }
    
    @Query( """
    select tp
    from TopicProblem tp
    where tp.problemId = :problemId
    """)
    TopicProblem findByProblemId( Integer problemId ) ;

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
          tp.problemState in ( 'Pigeon', 'Pigeon Solved' )
    order by tp.problemId
    """)
    List<TopicProblem> findPigeonedProblems( Integer topicId ) ;
    
    @Query( """
    select tp
    from TopicProblem tp
    where tp.problemState in ( 'Pigeon', 'Pigeon Solved' )
    order by tp.problemId
    """)
    List<TopicProblem> findAllPigeonedProblems() ;
    
    @Query( """
    select count(tp)
    from TopicProblem tp
    where tp.topicId = :topicId and
          tp.problemState in ( 'Pigeon', 'Pigeon Solved' )
    order by tp.problemId
    """)
    Integer findNumPigeonedProblems( Integer topicId ) ;
    
    @Query( """
    select
        tp.problemState as state,
        count( tp ) as numProblems
    from
        TopicProblem tp
    where
        tp.topicId = :topicId
    group by
        tp.problemState
    """ )
    List<ProblemStateCount> getProblemStateCounts( Integer topicId ) ;
    
    @Query( """
    select
        tp.problemState as state,
        count( tp ) as numProblems
    from
        TopicProblem tp
    where
        tp.topicId = :topicId and
        date( tp.lastAttemptTime ) = CURDATE()
    group by
        tp.problemState
    """ )
    List<ProblemStateCount> getProblemStateCountsForToday( Integer topicId ) ;
    
    @Query( nativeQuery = true, value = """
    select
        problem_state as state,
        count(*) as numProblems
    from
        topic_problems
    where
        topic_id = :topicId and
        last_attempt_time > ( select start_time from session where id = :sessionId )
    group by problem_state
    """ )
    List<ProblemStateCount> getProblemStateCountsForSession( Integer topicId, Integer sessionId ) ;
}
