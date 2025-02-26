package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table( name = "session_pause" )
public class SessionPause {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "session_id", nullable = false )
    private Session session;
    
    @Column( name = "start_time", nullable = false )
    private Instant startTime;
    
    @Column( name = "end_time", nullable = false )
    private Instant endTime;
    
}
