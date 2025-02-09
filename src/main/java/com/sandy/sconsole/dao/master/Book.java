package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "book_master" )
public class Book {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Column( name = "subject_name", nullable = false )
    private String subjectName;
    
    @Column( name = "book_name", nullable = false, length = 128 )
    private String bookName;
    
    @Column( name = "series_name", length = 128 )
    private String seriesName ;
    
    @Column( name = "author", nullable = false, length = 128 )
    private String author;
    
    @Column( name = "book_short_name", length = 64 )
    private String bookShortName;
    
    @Column( name = "topic_mapping_done" )
    private boolean topicMappingDone ;

    @OneToMany( mappedBy = "book" )
    private Set<Chapter> chapters = new LinkedHashSet<>();
}
