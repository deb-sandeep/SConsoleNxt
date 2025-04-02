package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.ProblemAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ProblemAttemptRepo extends JpaRepository<ProblemAttempt, Integer> {
    
    interface DayBurn {
        Date getDate() ;
        int getNumQuestionsSolved() ;
    }
    
    @Query( nativeQuery=true, value = """
            select
                date( start_time ) as date,
                count( problem_id ) as num_questions_solved
            from problem_attempt
            where
                topic_id = :topicId and
                target_state in ( 'Incorrect', 'Correct', 'Pigeon Explained', 'Purge' )
            group by date
            order by date
            """
    )
    List<DayBurn> getHistoricBurns( @Param( "topicId" ) Integer topicId ) ;

    @Query( nativeQuery=true, value = """
            select
                date( start_time ) as date,
                count( problem_id ) as num_questions_solved
            from problem_attempt
            where
                topic_id = :topicId and
                end_time > :startDate and
                target_state in ( 'Incorrect', 'Correct', 'Pigeon Explained', 'Purge' )
            group by date
            order by date
            """
    )
    List<DayBurn> getHistoricBurns( @Param( "startDate" ) Date startDate,
                                    @Param( "topicId" ) Integer topicId ) ;
}
