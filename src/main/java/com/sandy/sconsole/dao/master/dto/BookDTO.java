package com.sandy.sconsole.dao.master.dto;

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
    private boolean topicMappingDone ;
    
    public BookDTO(){}
    
    public BookDTO( Book book ) {
        this.id = book.getId() ;
        this.subjectName = book.getSubjectName() ;
        this.seriesName = book.getSeriesName() ;
        this.bookName = book.getBookName() ;
        this.author = book.getAuthor() ;
        this.bookShortName = book.getBookShortName() ;
        this.topicMappingDone = book.isTopicMappingDone() ;
    }
}
