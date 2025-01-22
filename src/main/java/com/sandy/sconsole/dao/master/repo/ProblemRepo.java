package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Problem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepo extends CrudRepository<Problem, Integer> {
    
    @Query( """
        select p
        from Problem p
        where
            p.chapter.book.id = :bookId and
            p.chapter.id.chapterNum = :chapterNum and
            p.exerciseNum = :exerciseNum
        order by
            p.id asc
    """ )
    List<Problem> getProblems( @Param( "bookId" ) int bookId,
                               @Param( "chapterNum" ) int chapterNum,
                               @Param( "exerciseNum" ) int exerciseNum ) ;
}
