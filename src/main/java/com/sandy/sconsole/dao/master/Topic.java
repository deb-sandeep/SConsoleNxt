package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "topic_master" )
public class Topic {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JoinColumn( name = "syllabus_name", nullable = false )
    private Syllabus syllabusName;
    
    @Column( name = "section", nullable = false, length = 64 )
    private String section;
    
    @Column( name = "topic_name", nullable = false, length = 256 )
    private String topicName;
    
    @OneToMany( mappedBy = "topic" )
    private Set<Subtopic> subtopics = new LinkedHashSet<>();
    
}
