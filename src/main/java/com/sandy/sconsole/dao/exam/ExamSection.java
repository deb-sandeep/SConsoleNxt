package com.sandy.sconsole.dao.exam;

import com.sandy.sconsole.dao.master.ProblemType;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.endpoints.rest.master.vo.ExamSectionVO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table( name = "exam_section" )
public class ExamSection {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "syllabus_name", nullable = false )
    private Syllabus syllabus;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "problem_type", nullable = false )
    private ProblemType problemType;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JoinColumn( name = "exam_id", nullable = false )
    private Exam exam;
    
    @Size( max = 128 )
    @NotNull
    @Column( name = "title", nullable = false, length = 128 )
    private String title;
    
    @NotNull
    @Column( name = "correct_marks", nullable = false )
    private Integer correctMarks;
    
    @NotNull
    @Column( name = "exam_sequence", nullable = false )
    private Integer examSequence;
    
    @NotNull
    @Column( name = "wrong_penalty", nullable = false )
    private Integer wrongPenalty;
    
    @NotNull
    @Column( name = "num_questions", nullable = false )
    private Integer numQuestions;
    
    @NotNull
    @Column( name = "num_compulsory_questions", nullable = false )
    private Integer numCompulsoryQuestions;
    
    @Size( max = 1024 )
    @NotNull
    @Column( name = "instructions", nullable = false, length = 1024 )
    private String instructions;
    
    @OneToMany( mappedBy = "section" )
    private Set<ExamQuestion> questions = new LinkedHashSet<>();
    
    public ExamSection() {}
    
    public ExamSection( ExamSectionVO vo, Syllabus syllabus, ProblemType problemType ) {
        this.id = null ;
        this.syllabus = syllabus ;
        this.problemType = problemType ;
        this.title = vo.getTitle() ;
        this.correctMarks = vo.getCorrectMarks() ;
        this.examSequence = vo.getExamSequence() ;
        this.wrongPenalty = vo.getWrongPenalty() ;
        this.numQuestions = vo.getNumQuestions() ;
        this.numCompulsoryQuestions = vo.getNumCompulsoryQuestions() ;
        this.instructions = vo.getInstructions() == null
                            ? ""
                            : String.join( "\n", vo.getInstructions() ) ;
    }
}
