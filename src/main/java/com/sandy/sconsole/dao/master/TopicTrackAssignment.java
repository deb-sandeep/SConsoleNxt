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
    
    @Column( name = "buffer_left", nullable = false )
    private Integer bufferLeft ;
    
    @Column( name = "buffer_right", nullable = false )
    private Integer bufferRight ;
    
    @Column( name = "theory_margin", nullable = false )
    private Integer theoryMargin ;
    
    @Column( name = "start_date", nullable = false )
    private Date startDate ;
    
    @Column( name = "end_date", nullable = false )
    private Date endDate ;
}
