package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "syllabus_master" )
public class Syllabus {
    @Id
    @Column( name = "syllabus_name", nullable = false, length = 64 )
    private String syllabusName;
    
    @ManyToOne( fetch = FetchType.EAGER, optional = false )
    @JoinColumn( name = "subject_name", nullable = false )
    private Subject subject;
    
    @OneToMany( mappedBy = "syllabus" )
    private Set<Topic> topics = new LinkedHashSet<>();
    
}
