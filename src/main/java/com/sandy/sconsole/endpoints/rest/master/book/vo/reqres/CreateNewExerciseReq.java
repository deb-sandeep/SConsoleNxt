package com.sandy.sconsole.endpoints.rest.master.book.vo.reqres;

import lombok.Data;

@Data
public class CreateNewExerciseReq {
    private int bookId ;
    private int chapterNum ;
    private String exerciseName ;
    private String problemClusters ;
}
