package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TagProblemMap;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagProblemMapRepo extends CrudRepository<TagProblemMap, Integer> {

    List<TagProblemMap> findByTagId( Integer tagId ) ;

    boolean existsByProblemIdAndTagId( Integer problemId, Integer tagId ) ;
}
