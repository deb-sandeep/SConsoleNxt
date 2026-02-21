package com.sandy.sconsole.dao.exam;

import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.Topic;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table( name = "exam_topics" )
public class ExamTopic {
    @Id
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JoinColumn( name = "exam_id", nullable = false )
    private Exam exam;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "syllabus_name", nullable = false )
    private Syllabus syllabusName;
    
    @NotNull
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "topic_id", nullable = false )
    private Topic topic;
}
