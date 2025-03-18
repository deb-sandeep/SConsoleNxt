package com.sandy.sconsole.dao.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Getter
@Setter
@Entity
@Immutable
@Table( name = "today_solved_problem_count" )
public class TodaySolvedProblemCount {
    
    @Id
    @Column( name = "topic_id", nullable = false )
    private Integer topicId;
    
    @Column( name = "num_solved_problems", nullable = false )
    private Long numSolvedProblems ;
}
