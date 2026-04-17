package com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres;

public record ExamNoteUpdateReq(
        Integer examId,
        String note
) {}
