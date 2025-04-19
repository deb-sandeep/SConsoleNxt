package com.sandy.sconsole.dao.master.dto;

import com.sandy.sconsole.dao.master.Chapter;
import lombok.Getter;

@Getter
public class ChapterDTO {

    private final int bookId ;
    private final int chapterNum ;
    private final String chapterName ;
    
    public ChapterDTO( Chapter chapter ) {
        this.bookId = chapter.getBook().getId() ;
        this.chapterNum = chapter.getId().getChapterNum() ;
        this.chapterName = chapter.getChapterName() ;
    }
}
