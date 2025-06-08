package com.sandy.sconsole.dao.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table( name = "session_event" )
public class SessionEvent {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Column( name = "event_id", nullable = false, length = 45 )
    private String eventId;
    
    @Column( name = "time", nullable = false )
    private Date time;
    
    @Column( name = "payload", nullable = false, length = 512 )
    private String payload;
}
