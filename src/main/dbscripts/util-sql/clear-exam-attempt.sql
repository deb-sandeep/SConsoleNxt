delete
from exam_question_attempt eqa
where
    eqa.exam_section_attempt_id in (
        select esa.id
        from exam_section_attempt esa
        where esa.exam_attempt_id in (
            select ea.id
            from exam_attempt ea
            where ea.exam_id = 1
        )
    ) ;

delete
from exam_section_attempt esa
where esa.exam_attempt_id in (
    select ea.id
    from exam_attempt ea
    where ea.exam_id = 1
) ;

delete
from exam_event_log eel
where eel.exam_attempt_id in (
    select ea.id
    from exam_attempt ea
    where ea.exam_id = 1
) ;


delete
from exam_attempt ea
where ea.exam_id = 1 ;

update exam
set state = 'PUBLISHED'
where id = 1 ;
