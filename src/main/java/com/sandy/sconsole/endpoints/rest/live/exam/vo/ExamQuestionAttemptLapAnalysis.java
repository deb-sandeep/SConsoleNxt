package com.sandy.sconsole.endpoints.rest.live.exam.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExamQuestionAttemptLapAnalysis {

    private String lapName ;
    private int score ;
    private String note ;
    private List<String> observations = new ArrayList<>() ;
}
