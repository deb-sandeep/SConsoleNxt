package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.TagQuestionMap;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagQuestionMapRepo extends CrudRepository<TagQuestionMap, Integer> {

    List<TagQuestionMap> findByTagId( Integer tagId ) ;

    boolean existsByQuestionIdAndTagId( Integer questionId, Integer tagId ) ;
}
