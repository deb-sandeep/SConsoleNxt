package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table( name = "subtopic_master" )
public class Subtopic {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JoinColumn( name = "topic_id", nullable = false )
    private Topic topic;
    
    @Column( name = "subtopic_name", nullable = false, length = 256 )
    private String subtopicName;
    
}
