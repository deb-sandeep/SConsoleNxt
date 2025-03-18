package com.sandy.sconsole.dao.session;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class DaySyllabusStudyTimeId implements Serializable {
    
    @Serial
    private static final long serialVersionUID = -8464943225337992252L;
    
    @Column( name = "date" )
    private Date date;
    
    @Column( name = "syllabus_name", nullable = false, length = 64 )
    private String syllabusName;
    
    @Override
    public boolean equals( Object o ) {
        if( this == o )
            return true;
        if( o == null || Hibernate.getClass( this ) != Hibernate.getClass( o ) )
            return false;
        DaySyllabusStudyTimeId entity = ( DaySyllabusStudyTimeId )o;
        return Objects.equals( this.date, entity.date ) && Objects.equals( this.syllabusName, entity.syllabusName );
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( date, syllabusName );
    }
}
