package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepo
        extends JpaRepository<Question, Integer>,
                JpaSpecificationExecutor<Question> {
    
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
            syllabus_name,
            topic_id,
            topic_name,
            problem_type,
            question_status as state,
            count
        from
            question_repo_status s
        order by
            syllabus_name,
            topic_id,
            problem_type
        """
    )
    List<RepoStatusRow> getRepoStatus() ;
}
