package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table( name = "exam_question_attempt" )
public class ExamQuestionAttempt {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_question_id", nullable = false )
    private ExamQuestion examQuestion;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_section_attempt_id", nullable = false )
    private ExamSectionAttempt examSectionAttempt;
    
    @NotNull
    @Column( name = "time_spent", nullable = false )
    private Integer timeSpent;
    
    @Lob
    @Column( name = "evaluation_status" )
    private String evaluationStatus;
    
    @Size( max = 128 )
    @Column( name = "answer_provided", length = 128 )
    private String answerProvided;
    
    @Size( max = 8 )
    @Column( name = "answer_submit_lap", length = 8 )
    private String answerSubmitLap;

    @NotNull
    @Lob
    @Column( name = "answer_submit_status", nullable = false )
    private String answerSubmitStatus;
    
    @NotNull
    @ColumnDefault( "0" )
    @Column( name = "score", nullable = false )
    private Integer score;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "root_cause" )
    private RootCauseMaster rootCause;
}
