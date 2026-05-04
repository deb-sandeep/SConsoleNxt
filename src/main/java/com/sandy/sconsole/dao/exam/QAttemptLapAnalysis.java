package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "exam_qattempt_lap_analysis" )
public class QAttemptLapAnalysis {
    
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JoinColumn( name = "attempt_id", nullable = false )
    private ExamQuestionAttempt attempt;
    
    @Size( max = 8 )
    @NotNull
    @Column( name = "lap_name", nullable = false, length = 8 )
    private String lapName;
    
    @Column( name = "score" )
    private Integer score;
    
    @OneToMany( mappedBy = "analysis" )
    private Set<QAttemptLapObs> observations = new LinkedHashSet<>();
    
    @Size( max = 256 )
    @Column( name = "note", length = 256 )
    private String note;
}
