package com.sandy.sconsole.dao.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table( name = "event_log" )
public class EventLog {
    
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Column( name = "time", nullable = false )
    private Date time;
    
    @Column( name = "title", nullable = false, length = 80 )
    private String title;
    
    @Column( name = "message", nullable = false, length = 256 )
    private String message;

    @Column( name = "message_type", nullable = false, length = 16 )
    private String messageType;
}
