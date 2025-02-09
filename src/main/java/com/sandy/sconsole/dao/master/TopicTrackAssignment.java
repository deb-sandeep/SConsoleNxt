package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table( name = "topic_track_assignment" )
public class TopicTrackAssignment {
    
    @Id
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
    private LocalDate startDate ;
    
    @Column( name = "end_date", nullable = false )
    private LocalDate endDate ;
}
