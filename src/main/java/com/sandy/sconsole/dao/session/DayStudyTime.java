package com.sandy.sconsole.dao.session;

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
@Table( name = "day_total_study_time" )
public class DayStudyTime {

    @Id
    @Column( name = "date" )
    private Date date;
    
    @Column( name = "total_time", precision = 32 )
    private BigDecimal totalTime;
}
