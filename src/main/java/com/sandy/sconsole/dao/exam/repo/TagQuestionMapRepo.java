package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.TagQuestionMap;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagQuestionMapRepo extends CrudRepository<TagQuestionMap, Integer> {

    List<TagQuestionMap> findByTagId( Integer tagId ) ;

    List<TagQuestionMap> findByQuestionId( Integer questionId ) ;

    boolean existsByQuestionIdAndTagId( Integer questionId, Integer tagId ) ;

    @Modifying
    @Query( "delete from TagQuestionMap m where m.question.id = :questionId and m.tag.id = :tagId" )
    void deleteByQuestionIdAndTagId( @Param( "questionId" ) Integer questionId, @Param( "tagId" ) Integer tagId ) ;

    @Modifying
    @Query( "delete from TagQuestionMap m where m.question.id = :questionId" )
    void deleteAllByQuestionId( @Param( "questionId" ) Integer questionId ) ;

    interface QuestionTagCount {
        Integer getQuestionId() ;
        Integer getCount() ;
    }

    @Query( nativeQuery = true, value = """
        select
            question_id as questionId,
            count(*) as count
        from tag_question_map
        where question_id in :questionIds
        group by question_id
    """ )
    List<QuestionTagCount> countTagsByQuestionIds( @Param( "questionIds" ) List<Integer> questionIds ) ;
}
