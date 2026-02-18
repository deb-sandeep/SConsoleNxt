package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table( name = "exam_attempt" )
public class ExamAttempt {
    @Id
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @OneToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_id", nullable = false )
    private Exam exam;
    
    @NotNull
    @Column( name = "attempt_date", nullable = false )
    private Instant attemptDate;
    
    @NotNull
    @ColumnDefault( "0" )
    @Column( name = "score", nullable = false )
    private Integer score;
    
    @NotNull
    @Column( name = "avoidable_loss_pct", nullable = false )
    private Float avoidableLossPct;
    
    @NotNull
    @Column( name = "unavoidable_loss_pct", nullable = false )
    private Float unavoidableLossPct;
    
    @Lob
    @Column( name = "status" )
    private String status;
    
    
}
