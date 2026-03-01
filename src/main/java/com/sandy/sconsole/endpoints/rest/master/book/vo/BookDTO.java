package com.sandy.sconsole.endpoints.rest.master.book.vo;

import com.sandy.sconsole.dao.master.Book;
import lombok.Data;

@Data
public class BookDTO {
    
    private int id ;
    private String syllabusName ;
    private String subjectName ;
    private String seriesName ;
    private String bookName ;
    private String author ;
    private String bookShortName ;
    private Boolean extensible ;
    private boolean topicMappingDone ;
    
    public BookDTO(){}
    
    public BookDTO( Book book ) {
        this.id = book.getId() ;
        this.subjectName = book.getSubjectName() ;
        this.seriesName = book.getSeriesName() ;
        this.bookName = book.getBookName() ;
        this.author = book.getAuthor() ;
        this.bookShortName = book.getBookShortName() ;
        this.extensible = book.isExtensible() ;
        this.topicMappingDone = book.isTopicMappingDone() ;
    }
}
