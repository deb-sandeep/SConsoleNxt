package com.sandy.sconsole.dao.session;

import com.sandy.sconsole.dao.master.Topic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "session" )
public class Session {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Column( name = "start_time", nullable = false )
    private Instant startTime;
    
    @Column( name = "end_time", nullable = false )
    private Instant endTime;
    
    @JoinColumn( name = "session_type", nullable = false )
    private String sessionType;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "topic_id", nullable = false )
    private Topic topic;
    
    @JoinColumn( name = "syllabus_name", nullable = false )
    private String syllabusName ;
    
    @OneToMany( mappedBy = "session" )
    private Set<ProblemAttempt> problemAttempts = new LinkedHashSet<>();
    
    @OneToMany( mappedBy = "session" )
    private Set<SessionPause> pauses = new LinkedHashSet<>();
    
    @ColumnDefault( "0" )
    @Column( name = "effective_duration", nullable = false )
    private Integer effectiveDuration;
}
