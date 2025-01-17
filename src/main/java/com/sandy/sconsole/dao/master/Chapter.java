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
@Table( name = "chapter_master" )
public class Chapter {
    
    @EmbeddedId
    private ChapterId id;
    
    @MapsId( "bookId" )
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JoinColumn( name = "book_id", nullable = false )
    private Book book;
    
    @Column( name = "chapter_name", nullable = false )
    private String chapterName;
    
    @OneToMany( mappedBy = "chapter" )
    private Set<Problem> problems = new LinkedHashSet<>();
}
