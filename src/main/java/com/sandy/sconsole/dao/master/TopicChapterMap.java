package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table( name = "topic_chapter_map" )
public class TopicChapterMap {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JoinColumn( name = "topic_id", nullable = false )
    private Topic topic;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumns( { @JoinColumn( name = "book_id", referencedColumnName = "book_id", nullable = false ), @JoinColumn( name = "chapter_num", referencedColumnName = "chapter_num", nullable = false ) } )
    @OnDelete( action = OnDeleteAction.CASCADE )
    private Chapter chapter;
    
}
