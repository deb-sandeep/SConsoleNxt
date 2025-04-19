package com.sandy.sconsole.endpoints.rest.master.vo.reqres;

import lombok.Data;

@Data
public class CreateNewExerciseReq {
    private int bookId ;
    private int chapterNum ;
    private String exerciseName ;
    private String problemClusters ;
}
