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
    
    @Query( """
        select p, tcpm
        from Problem p
            left outer join TopicChapterProblemMap tcpm
                on tcpm.problem = p
            left outer join TopicChapterMap tcm
                on tcm = tcpm.topicChapterMap
        where
            p.chapter.book.id = :bookId and
            p.chapter.id.chapterNum = :chapterNum
        order by
            p.exerciseNum asc,
            p.id asc
    """)
    List<Object[]> getProblemTopicMappings( @Param( "bookId" ) int bookId,
                                            @Param( "chapterNum" ) int chapterNum ) ;
    
    @Query( """
        select p
        from Problem p
        where
            p.chapter.book.id = :bookId and
            p.chapter.id.chapterNum = :chapterNum and
            p not in (
                select tcpm.problem
                from TopicChapterProblemMap tcpm
                where
                    tcpm.problem.chapter.book.id = :bookId and
                    tcpm.problem.chapter.id.chapterNum = :chapterNum
            )
        order by
            p.exerciseNum asc,
            p.id asc
    """)
    List<Problem> getUnassociatedProblemsForChapter( @Param( "bookId" ) int bookId,
                                                     @Param( "chapterNum" ) int chapterNum ) ;

    
    @Query( """
        select CASE WHEN( max( p.exerciseNum ) IS NULL ) THEN 1 ELSE max( p.exerciseNum )+1 END
        from Problem p
        where
            p.chapter.book.id = :bookId and
            p.chapter.id.chapterNum = :chapterNum
    """ )
    Integer getNextExerciseNum( @Param( "bookId" ) int bookId,
                                @Param( "chapterNum" ) int chapterNum ) ;
}
