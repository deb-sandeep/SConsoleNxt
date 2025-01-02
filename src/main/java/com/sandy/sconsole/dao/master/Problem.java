package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table( name = "problem_master" )
public class Problem {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumns( { @JoinColumn( name = "book_id", referencedColumnName = "book_id", nullable = false ), @JoinColumn( name = "chapter_num", referencedColumnName = "chapter_num", nullable = false ) } )
    @OnDelete( action = OnDeleteAction.CASCADE )
    private Chapter chapter;
    
    @Column( name = "exercise_name", nullable = false, length = 64 )
    private String exerciseName;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "problem_type", nullable = false )
    private ProblemType problemType;
    
    @Column( name = "problem_id", nullable = false, length = 64 )
    private String problemId;
    
}
