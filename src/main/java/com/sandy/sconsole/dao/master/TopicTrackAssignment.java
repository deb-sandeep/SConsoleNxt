package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@Table( name = "topic_track_assignment" )
public class TopicTrackAssignment {
    
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id ;
    
    @Column( name = "track_id" )
    private Integer trackId ;
    
    @Column( name = "sequence", nullable = false )
    private Integer sequence;
    
    @Column( name = "topic_id", nullable = false )
    private Integer topicId ;
    
    @Column( name = "start_date", nullable = false )
    private Date startDate ;
    
    @Column( name = "coaching_num_days", nullable = false )
    private Integer coachingNumDays ;
    
    @Column( name = "self_study_num_days", nullable = false )
    private Integer selfStudyNumDays ;
    
    @Column( name = "consolidation_num_days", nullable = false )
    private Integer consolidationNumDays ;
    
    @Column( name = "end_date", nullable = false )
    private Date endDate ;
    
    @Column( name = "inter_topic_gap_num_days", nullable = false )
    private Integer interTopicGapNumDays ;
}
