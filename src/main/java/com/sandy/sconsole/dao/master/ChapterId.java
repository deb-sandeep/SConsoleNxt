package com.sandy.sconsole.dao.master;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ChapterId implements Serializable {
    
    @Serial
    private static final long serialVersionUID = -401073245037941796L;
    
    @Column( name = "book_id", nullable = false )
    private Integer bookId;
    
    @Column( name = "chapter_num", nullable = false )
    private Integer chapterNum;
    
    public ChapterId() {}
    
    public ChapterId( int bookId, int chapterNum ) {
        this.bookId = bookId ;
        this.chapterNum = chapterNum ;
    }
    
    @Override
    public boolean equals( Object o ) {
        if( this == o )
            return true;
        if( o == null || Hibernate.getClass( this ) != Hibernate.getClass( o ) )
            return false;
        ChapterId entity = ( ChapterId )o;
        return Objects.equals( this.bookId, entity.bookId ) &&
               Objects.equals( this.chapterNum, entity.chapterNum );
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( bookId, chapterNum );
    }
    
}
