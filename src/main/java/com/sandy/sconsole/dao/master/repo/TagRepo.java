package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TagMaster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepo extends CrudRepository<TagMaster, Integer> {

    Optional<TagMaster> findByNormalizedTagText( String normalizedTagText ) ;

    List<TagMaster> findByTopicId( Integer topicId ) ;

    List<TagMaster> findAllByOrderByTagTextAsc() ;

    @Query( """
        select t
        from TagMaster t
        where t.normalizedTagText like concat( '%', :fragment, '%' )
        order by t.tagText
    """ )
    List<TagMaster> searchByText( @Param( "fragment" ) String fragment ) ;

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
}
