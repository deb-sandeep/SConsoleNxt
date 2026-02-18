package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "exam_question_attempt" )
public class ExamQuestionAttempt {
    @Id
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_question_id", nullable = false )
    private ExamQuestion examQuestion;
    
    @NotNull
    @Column( name = "time_spent", nullable = false )
    private Integer timeSpent;
    
    @Lob
    @Column( name = "evaluation_status" )
    private String evaluationStatus;
    
    @Size( max = 128 )
    @Column( name = "answer_provided", length = 128 )
    private String answerProvided;
    
    
}
