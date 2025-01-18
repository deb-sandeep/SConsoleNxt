package com.sandy.sconsole.api.master.dto;

public interface BookSummary {
    
    Integer getId() ;
    String getSubjectName() ;
    String getSeriesName() ;
    String getBookName() ;
    String getAuthor() ;
    String getBookShortName() ;
    int getNumChapters() ;
    int getNumProblems() ;
}
