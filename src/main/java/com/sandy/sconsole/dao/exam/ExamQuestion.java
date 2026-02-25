package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "exam_question" )
public class ExamQuestion {
    
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @OneToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "question_id", nullable = false )
    private Question question;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "exam_section_id", nullable = false )
    private ExamSection section;
    
    @NotNull
    @Column( name = "sequence", nullable = false )
    private Integer sequence;
    
    public ExamQuestion() {}
    
    public ExamQuestion( Integer id, Question question,
                         ExamSection examSection, int sequence ) {
        this.id = id;
        this.question = question;
        this.section = examSection;
        this.sequence = sequence;
    }
}
