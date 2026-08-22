package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "tag_problem_map" )
public class TagProblemMap {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;

    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "problem_id", nullable = false )
    private Problem problem;

    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "tag_id", nullable = false )
    private Tag tag;
}
