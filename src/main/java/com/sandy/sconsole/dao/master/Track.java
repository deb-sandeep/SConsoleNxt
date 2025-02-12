package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "track_master" )
public class Track {
    
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Column( name = "track_name", nullable = false, length = 64 )
    private String trackName;
    
    @Column( name = "color", length = 8 )
    private String color;
    
    @Column( name = "syllabus_name" )
    private String syllabusName;
    
    @OneToMany( mappedBy = "topicId" )
    @OrderBy( "sequence" )
    private Set<TopicTrackAssignment> assignedTopics = new LinkedHashSet<>();
    
    @Column( name = "start_date" )
    private LocalDate startDate;
}
