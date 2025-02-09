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
    
    @Column( name = "subject_name", nullable = false )
    private String subjectName;
    
    @Column( name = "color", nullable = false, length = 8 )
    private String color;

    @OneToMany( mappedBy = "syllabus" )
    @OrderBy( "id" )
    private Set<Topic> topics = new LinkedHashSet<>();
}
