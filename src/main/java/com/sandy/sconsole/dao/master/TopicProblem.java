package com.sandy.sconsole.dao.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Immutable
@Table( name = "topic_problems" )
public class TopicProblem {
    
    @Column( name = "topic_id" )
    private Integer topicId;
    
    @Column( name = "syllabus_name", length = 64 )
    private String syllabusName;
    
    @Column( name = "topic_section", length = 64 )
    private String topicSection;
    
    @Column( name = "topic_name", length = 256 )
    private String topicName;
    
    @Column( name = "book_id", nullable = false )
    private Integer bookId;
    
    @Column( name = "book_short_name", length = 64 )
    private String bookShortName;
    
    @Column( name = "book_series", length = 128 )
    private String bookSeries;
    
    @Column( name = "chapter_num", nullable = false )
    private Integer chapterNum;
    
    @Column( name = "chapter_name", length = 128 )
    private String chapterName;
    
    @Id
    @Column( name = "problem_id", nullable = false )
    private Integer problemId;
    
    @Column( name = "exercise_num", nullable = false )
    private Integer exerciseNum;
    
    @Column( name = "exercise_name", nullable = false, length = 64 )
    private String exerciseName;
    
    @Column( name = "problem_type", nullable = false, length = 8 )
    private String problemType;
    
    @Column( name = "problem_key", nullable = false, length = 64 )
    private String problemKey;
    
    @Column( name = "problem_state", length = 32 )
    private String problemState;
    
    @Column( name = "difficulty_level", nullable = false )
    private Integer difficultyLevel;
    
    @Column( name = "last_attempt_time" )
    private Date lastAttemptTime;
    
    @Column( name = "total_duration", precision = 32 )
    private BigDecimal totalDuration;
    
    @Column( name = "num_attempts" )
    private Long numAttempts;
}
