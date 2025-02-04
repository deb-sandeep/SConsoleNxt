package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TopicChapterProblemMap;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicChapterProblemMapRepo extends CrudRepository<TopicChapterProblemMap, Integer> {

    interface TCProblemTypeCount {
        int getMappingId() ;
        String getProblemType() ;
        int getCount() ;
    }
    
    @Query( """
        select
            tcpm.topicChapterMap.id as mappingId,
            p.problemType.problemType as problemType,
            count( p ) as count
        from TopicChapterProblemMap tcpm
            left outer join Problem p
                on tcpm.problem = p
        where
            tcpm.topicChapterMap.id in :tcmIds
        group by
            tcpm.topicChapterMap.id,
            p.problemType.problemType
    """)
    List<TCProblemTypeCount> getTopicChapterProblemTypeCounts( @Param( "tcmIds" ) Integer[] tcmIds ) ;
    
}
