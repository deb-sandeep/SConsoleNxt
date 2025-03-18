package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.TodaySolvedProblemCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodaySolvedProblemCountRepo extends JpaRepository<TodaySolvedProblemCount, Integer> {

    @Query( """
    select tspc.numSolvedProblems
    from TodaySolvedProblemCount tspc
    where tspc.topicId = :topicId
    """)
    Integer getNumSolvedProblems( @Param( "topicId" ) Integer topicId ) ;
}
