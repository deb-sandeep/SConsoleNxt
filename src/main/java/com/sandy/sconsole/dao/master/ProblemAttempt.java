package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table( name = "problem_attempt" )
public class ProblemAttempt {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "session_id", nullable = false )
    private com.sandy.sconsole.dao.master.Session session;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "problem_id", nullable = false )
    private Problem problem;
    
    @Column( name = "start_time", nullable = false )
    private Instant startTime;
    
    @Column( name = "end_time", nullable = false )
    private Instant endTime;
    
    @ColumnDefault( "0" )
    @Column( name = "effective_duration", nullable = false )
    private Integer effectiveDuration;
}
