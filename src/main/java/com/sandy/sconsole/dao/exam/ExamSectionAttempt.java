package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "exam_section_attempt" )
public class ExamSectionAttempt {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_section_id", nullable = false )
    private ExamSection examSection;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_attempt_id", nullable = false )
    private ExamAttempt examAttempt;
    
    @NotNull
    @Column( name = "score", nullable = false )
    private Integer score;
    
    @Column( name = "loss" )
    private Integer loss;
    
    @Column( name = "avoidable_loss" )
    private Integer avoidableLoss;
    
    @NotNull
    @Column( name = "avoidable_loss_pct", nullable = false )
    private Float avoidableLossPct;
    
    @OneToMany( mappedBy = "examSectionAttempt", fetch = FetchType.EAGER )
    private Set<ExamQuestionAttempt> questionAttempts = new LinkedHashSet<>();
}
