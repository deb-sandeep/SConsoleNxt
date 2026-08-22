package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepo extends CrudRepository<Tag, Integer> {

    Optional<Tag> findByNormalizedTagText( String normalizedTagText ) ;

    List<Tag> findByTopicId( Integer topicId ) ;

    List<Tag> findAllByOrderByTagTextAsc() ;

    @Query( """
        select t
        from Tag t
        where t.normalizedTagText like concat( '%', :fragment, '%' )
        order by t.tagText
    """ )
    List<Tag> searchByText( @Param( "fragment" ) String fragment ) ;

    @Query( nativeQuery = true, value = """
        select t.id
        from tag_master t
            left join (
                select tag_id, count(*) as cnt from tag_problem_map group by tag_id
            ) pc on pc.tag_id = t.id
            left join (
                select tag_id, count(*) as cnt from tag_question_map group by tag_id
            ) qc on qc.tag_id = t.id
        order by
            ( coalesce( pc.cnt, 0 ) + coalesce( qc.cnt, 0 ) ) desc,
            t.tag_text
        limit :limit
    """ )
    List<Integer> findMostUsedTagIds( @Param( "limit" ) int limit ) ;

    interface TagAssociationCount {
        Integer getTagId() ;
        Integer getAssociationCount() ;
    }

    @Query( nativeQuery = true, value = """
        select
            t.id as tagId,
            ( coalesce( pc.cnt, 0 ) + coalesce( qc.cnt, 0 ) ) as associationCount
        from tag_master t
            left join (
                select tag_id, count(*) as cnt from tag_problem_map group by tag_id
            ) pc on pc.tag_id = t.id
            left join (
                select tag_id, count(*) as cnt from tag_question_map group by tag_id
            ) qc on qc.tag_id = t.id
        where t.topic_id = :topicId
    """ )
    List<TagAssociationCount> findAssociationCountsByTopicId( @Param( "topicId" ) Integer topicId ) ;
}
