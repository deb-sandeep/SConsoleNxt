package com.sandy.sconsole.endpoints.rest.master.core.vo;

import com.sandy.sconsole.dao.master.Chapter;
import lombok.Getter;

@Getter
public class ChapterVO {

    private final int bookId ;
    private final int chapterNum ;
    private final String chapterName ;
    
    public ChapterVO( Chapter chapter ) {
        this.bookId = chapter.getBook().getId() ;
        this.chapterNum = chapter.getId().getChapterNum() ;
        this.chapterName = chapter.getChapterName() ;
    }
}
