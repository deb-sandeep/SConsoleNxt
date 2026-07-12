package com.sandy.sconsole.dao.session;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DailyBurnLogId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1451018664020864951L;

    @Column( name = "date" )
    private Date date;

    @Column( name = "topic_id", nullable = false )
    private Integer topicId;

    @Override
    public boolean equals( Object o ) {
        if( this == o )
            return true;
        if( o == null || Hibernate.getClass( this ) != Hibernate.getClass( o ) )
            return false;
        DailyBurnLogId entity = ( DailyBurnLogId )o;
        return Objects.equals( this.date, entity.date ) && Objects.equals( this.topicId, entity.topicId );
    }

    @Override
    public int hashCode() {
        return Objects.hash( date, topicId );
    }
}
