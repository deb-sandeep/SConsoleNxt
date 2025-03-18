package com.sandy.sconsole.dao.session;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Immutable
@Table( name = "day_syllabus_total_study_time" )
public class DaySyllabusStudyTime {

    @EmbeddedId
    private DaySyllabusStudyTimeId id;
    
    @Column( name = "total_time", precision = 32 )
    private BigDecimal totalTime;
}
