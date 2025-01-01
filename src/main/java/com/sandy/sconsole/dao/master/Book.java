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
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "subject_name", nullable = false )
    private Subject subjectName;
    
    @Column( name = "book_name", nullable = false, length = 128 )
    private String bookName;
    
    @Column( name = "author", nullable = false, length = 128 )
    private String author;
    
    @Column( name = "book_short_name", length = 64 )
    private String bookShortName;
    
    @Column( name = "acronym", nullable = false, length = 8 )
    private String acronym;
    
    @OneToMany( mappedBy = "book" )
    private Set<Chapter> chapters = new LinkedHashSet<>();
    
}
