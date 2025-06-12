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
    
    interface ProblemState {
        int getProblemId() ;
        String getState() ;
    }
    
    @Query( """
    select pa
    from ProblemAttempt pa
    where pa.problem.id = :problemId
    order by pa.startTime
    """)
    List<ProblemAttempt> getProblemAttempts( @Param( "problemId" ) Integer problemId ) ;
    
    @Query( """
    select sum(pa.effectiveDuration)
    from ProblemAttempt pa
    where pa.problem.id = :problemId
    """)
    Integer getTotalAttemptTime( @Param( "problemId" ) Integer problemId ) ;
    
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
    
    @Query( nativeQuery = true, value = """
            select
                p.id as problemId,
                (IF((lps.state IS NULL), 'Assigned', lps.state)) AS state
            from
                latest_problem_state lps
                right outer join problem_master p
                    on p.id = lps.problem_id\s
                where
                    p.book_id = :bookId and
                    p.chapter_num = :chapterNum
    """)
    List<ProblemState> getProblemState( @Param( "bookId" ) Integer bookId,
                                        @Param( "chapterNum" ) Integer chapterNum ) ;
}
