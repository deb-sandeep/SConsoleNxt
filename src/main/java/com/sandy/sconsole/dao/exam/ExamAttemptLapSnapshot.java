package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "exam_attempt_lap_snapshot" )
public class ExamAttemptLapSnapshot {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @Column( name = "exam_attempt_id", nullable = false )
    private Integer examAttemptId;
    
    @NotNull
    @Column( name = "exam_question_id", nullable = false )
    private Integer examQuestionId;
    
    @Size( max = 8 )
    @NotNull
    @Column( name = "lap_name", nullable = false, length = 8 )
    private String lapName;
    
    @NotNull
    @Column( name = "time_spent", nullable = false )
    private Integer timeSpent;
    
    @Size( max = 32 )
    @NotNull
    @Column( name = "attempt_status", nullable = false, length = 32 )
    private String attemptStatus;
}
