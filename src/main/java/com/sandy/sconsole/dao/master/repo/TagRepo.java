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
}
