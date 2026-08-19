package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TagProblemMap;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagProblemMapRepo extends CrudRepository<TagProblemMap, Integer> {

    List<TagProblemMap> findByTagId( Integer tagId ) ;

    List<TagProblemMap> findByProblemId( Integer problemId ) ;

    boolean existsByProblemIdAndTagId( Integer problemId, Integer tagId ) ;

    @Modifying
    @Query( "delete from TagProblemMap m where m.problem.id = :problemId and m.tag.id = :tagId" )
    void deleteByProblemIdAndTagId( @Param( "problemId" ) Integer problemId, @Param( "tagId" ) Integer tagId ) ;

    @Modifying
    @Query( "delete from TagProblemMap m where m.problem.id = :problemId" )
    void deleteAllByProblemId( @Param( "problemId" ) Integer problemId ) ;
}
