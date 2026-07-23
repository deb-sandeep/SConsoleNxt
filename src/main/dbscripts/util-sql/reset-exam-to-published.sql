-- Erases all attempt/session data for a given exam and resets its state
-- back to PUBLISHED, leaving the exam's configured content (exam_section,
-- exam_question, exam_topics) untouched.
--
-- Edit the exam id below, then run the whole script.

SET @exam_id = 16;

START TRANSACTION;

delete
from exam_qattempt_lap_obs eqlo
where eqlo.analysis_id in (
    select eqla.id
    from exam_qattempt_lap_analysis eqla
    where eqla.attempt_id in (
        select eqa.id
        from exam_question_attempt eqa
        where eqa.exam_section_attempt_id in (
            select esa.id
            from exam_section_attempt esa
            where esa.exam_attempt_id in (
                select ea.id
                from exam_attempt ea
                where ea.exam_id = @exam_id
            )
        )
    )
) ;

delete
from exam_qattempt_lap_analysis eqla
where eqla.attempt_id in (
    select eqa.id
    from exam_question_attempt eqa
    where eqa.exam_section_attempt_id in (
        select esa.id
        from exam_section_attempt esa
        where esa.exam_attempt_id in (
            select ea.id
            from exam_attempt ea
            where ea.exam_id = @exam_id
        )
    )
) ;

delete
from exam_question_attempt eqa
where eqa.exam_section_attempt_id in (
    select esa.id
    from exam_section_attempt esa
    where esa.exam_attempt_id in (
        select ea.id
        from exam_attempt ea
        where ea.exam_id = @exam_id
    )
) ;

delete
from exam_attempt_lap_snapshot eals
where eals.exam_attempt_id in (
    select ea.id
    from exam_attempt ea
    where ea.exam_id = @exam_id
) ;

delete
from exam_section_attempt esa
where esa.exam_attempt_id in (
    select ea.id
    from exam_attempt ea
    where ea.exam_id = @exam_id
) ;

delete
from exam_event_log eel
where eel.exam_attempt_id in (
    select ea.id
    from exam_attempt ea
    where ea.exam_id = @exam_id
) ;

delete
from exam_attempt ea
where ea.exam_id = @exam_id ;

update exam
set state = 'PUBLISHED'
where id = @exam_id ;

COMMIT;

