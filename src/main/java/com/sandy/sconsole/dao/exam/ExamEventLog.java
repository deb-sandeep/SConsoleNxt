package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table( name = "exam_event_log" )
public class ExamEventLog {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_attempt_id", nullable = false )
    private ExamAttempt examAttempt;
    
    @NotNull
    @Column( name = "sequence", nullable = false )
    private Integer sequence;
    
    @Size( max = 20 )
    @NotNull
    @Column( name = "event_type", nullable = false, length = 20 )
    private String eventType;

    @Size( max = 32 )
    @NotNull
    @Column( name = "event_name", nullable = false, length = 32 )
    private String eventName;
    
    @NotNull
    @Column( name = "payload", nullable = false )
    private String payload;
    
    @NotNull
    @Column( name = "creation_time", nullable = false )
    private Instant creationTime;
    
    @NotNull
    @Column( name = "time_marker", nullable = false )
    private Long timeMarker;
}
