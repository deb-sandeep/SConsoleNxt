package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.ProblemAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface ProblemAttemptRepo extends JpaRepository<ProblemAttempt, Integer> {
    
    interface DayBurnStat {
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
                target_state in ( 'Incorrect', 'Correct', 'Pigeon Kill', 'Purge' )
            group by date
            order by date
            """
    )
    List<DayBurnStat> getHistoricBurnStats( Integer topicId ) ;
}
