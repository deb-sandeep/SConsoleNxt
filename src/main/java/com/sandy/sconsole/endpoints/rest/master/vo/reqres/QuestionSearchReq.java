package com.sandy.sconsole.endpoints.rest.master.vo.reqres;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record QuestionSearchReq(
    List<Integer> topicIds,
    
    @Min(0)
    Integer page,
    
    @Min(1) @Max(50)
    Integer size,
    
    List<String> sort
){}
