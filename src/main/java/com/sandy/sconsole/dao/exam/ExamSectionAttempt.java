package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "exam_section_attempt" )
public class ExamSectionAttempt {
    @Id
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
    
    @NotNull
    @Column( name = "avoidable_loss_pct", nullable = false )
    private Float avoidableLossPct;
    
    @NotNull
    @Column( name = "unavoidable_loss_pct", nullable = false )
    private Float unavoidableLossPct;
    
    
}
