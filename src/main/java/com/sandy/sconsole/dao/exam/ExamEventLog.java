package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table( name = "exam_event_log" )
public class ExamEventLog {
    @Id
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @Column( name = "sequence", nullable = false )
    private Integer sequence;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_attempt_id", nullable = false )
    private ExamAttempt examAttempt;
    
    @Size( max = 32 )
    @NotNull
    @Column( name = "event_id", nullable = false, length = 32 )
    private String eventId;
    
    @NotNull
    @JdbcTypeCode( SqlTypes.JSON )
    @Column( name = "payload", nullable = false )
    private String payload;
    
    @NotNull
    @Column( name = "creation_time", nullable = false )
    private Instant creationTime;
    
    @NotNull
    @Column( name = "time_marker", nullable = false )
    private Integer timeMarker;
    
    
}
