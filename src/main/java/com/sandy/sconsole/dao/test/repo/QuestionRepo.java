package com.sandy.sconsole.dao.test.repo;

import com.sandy.sconsole.dao.test.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepo extends JpaRepository<Question, Integer> {
    
    interface RepoStatusRow {
        String getSyllabusName() ;
        int getTopicId() ;
        String getTopicName() ;
        String getProblemType() ;
        String getState() ;
        int getCount() ;
    }
    
    Question findByQuestionId( String questionId ) ;
    
    @Query( nativeQuery=true, value = """
        select
            q.syllabus_name,
            q.topic_id,
            t.topic_name,
            q.problem_type,
            q.state,
            count(q.id) as count
        from
            question q
            left outer join
                topic_master t
                on q.topic_id = t.id
        group by
            q.syllabus_name,
            q.topic_id,
            q.problem_type,
            q.state
        order by
            q.syllabus_name,
            q.topic_id,
            q.problem_type,
            q.state
        """
    )
    List<RepoStatusRow> getRepoStatus() ;
}
