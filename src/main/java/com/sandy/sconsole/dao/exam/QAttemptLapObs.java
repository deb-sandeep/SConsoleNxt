package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table( name = "exam_qattempt_lap_obs" )
public class QAttemptLapObs {
    
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JoinColumn( name = "analysis_id", nullable = false )
    private QAttemptLapAnalysis analysis;
    
    @NotNull
    @Column( name = "observation", nullable = false )
    private String observation;
}
