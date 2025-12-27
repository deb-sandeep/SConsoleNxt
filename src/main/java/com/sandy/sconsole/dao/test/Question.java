package com.sandy.sconsole.dao.test;

import com.sandy.sconsole.dao.master.ProblemType;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.Topic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "question" )
public class Question {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Column( name = "question_id", nullable = false, length = 100 )
    private String questionId;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "syllabus_name", nullable = false )
    private Syllabus syllabus ;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "topic_id", nullable = false )
    private Topic topic;
    
    @Column( name = "source_id", nullable = false, length = 128 )
    private String sourceId;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "problem_type", nullable = false )
    private ProblemType problemType;
    
    @Column( name = "lct_sequence" )
    private Integer lctSequence;
    
    @Column( name = "question_number", nullable = false )
    private Integer questionNumber;
    
    @Column( name = "answer", nullable = false, length = 128 )
    private String answer;
    
    @Column( name = "server_sync_time", nullable = false )
    private Instant serverSyncTime;
    
    @OneToMany( mappedBy = "question", fetch = FetchType.EAGER )
    private Set<QuestionImage> questionImages = new LinkedHashSet<>();
}
