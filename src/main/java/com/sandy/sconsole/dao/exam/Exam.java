package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "exam" )
public class Exam {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Size( max = 8 )
    @NotNull
    @Column( name = "type", nullable = false, length = 8 )
    private String type;
    
    @Size( max = 256 )
    @Column( name = "note", length = 256 )
    private String note;
    
    @NotNull
    @Column( name = "num_phy_questions", nullable = false )
    private Integer numPhyQuestions;
    
    @NotNull
    @Column( name = "num_chem_questions", nullable = false )
    private Integer numChemQuestions;
    
    @NotNull
    @Column( name = "num_math_questions", nullable = false )
    private Integer numMathQuestions;
    
    @NotNull
    @Column( name = "total_marks", nullable = false )
    private Integer totalMarks;
    
    @NotNull
    @Column( name = "duration", nullable = false )
    private Integer duration;
    
    @NotNull
    @Column( name = "creation_date", nullable = false )
    private Instant creationDate;
    
    @OneToMany( mappedBy = "exam" )
    private Set<ExamSection> sections = new LinkedHashSet<>();
    
    @OneToMany( mappedBy = "exam" )
    private Set<ExamTopic> topics = new LinkedHashSet<>();
}
