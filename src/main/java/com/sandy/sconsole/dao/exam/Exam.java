package com.sandy.sconsole.dao.exam;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.master.ProblemType;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.repo.ProblemTypeRepo;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.endpoints.rest.master.core.vo.TopicVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamSectionVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamVO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
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
    
    @NotNull
    @Lob
    @Column( name = "state", nullable = false )
    private String state;

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
    
    @OneToMany( mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true )
    @OrderBy( "examSequence" )
    private Set<ExamSection> sections = new LinkedHashSet<>();
    
    @OneToMany( mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true )
    private Set<ExamTopic> topics = new LinkedHashSet<>();
    
    public Exam() {}
    
    public Exam( ExamVO vo ) {
        this.state = vo.getState() ;
        this.type = vo.getType() ;
        this.note = vo.getNote() ;
        this.numPhyQuestions = vo.getNumPhyQuestions() ;
        this.numChemQuestions = vo.getNumChemQuestions() ;
        this.numMathQuestions = vo.getNumMathQuestions() ;
        this.totalMarks = vo.getTotalMarks() ;
        this.duration = vo.getDuration() ;
        this.creationDate = Instant.now() ;
        
        SyllabusRepo syllabusRepo = SConsole.getBean( SyllabusRepo.class ) ;
        TopicRepo topicRepo = SConsole.getBean( TopicRepo.class ) ;
        ProblemTypeRepo ptRepo = SConsole.getBean( ProblemTypeRepo.class ) ;
        
        List<ExamSectionVO> sectionVOs ;
        List<List<TopicVO>> topicGroups ;
        
        sectionVOs = vo.getSections() ;
        for( ExamSectionVO sectionVO : sectionVOs ) {
            Syllabus syllabus = syllabusRepo.findById( sectionVO.getSyllabusName() ).get() ;
            ProblemType pt = ptRepo.findById( sectionVO.getProblemType() ).get() ;

            ExamSection section = new ExamSection( sectionVO, syllabus, pt ) ;
            section.setExam( this ) ;
            this.sections.add( section ) ;
        }
        
        topicGroups = vo.getTopics().values().stream().toList() ;
        for( List<TopicVO> topicVOList : topicGroups ) {
            for( TopicVO topicVO : topicVOList ) {
                Topic topic = topicRepo.findById( topicVO.getId() ).get() ;

                ExamTopic examTopic = new ExamTopic( topic ) ;
                examTopic.setExam( this ) ;
                this.topics.add( examTopic ) ;
            }
        }
    }
}
