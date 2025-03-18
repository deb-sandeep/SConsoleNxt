create view day_total_study_time as
select
    day_study_time.date as date,
    sum(day_study_time.effective_duration) as total_time
from
    (
        select
            DATE(end_time) as date,
            effective_duration
        from session
    ) as day_study_time
group by date
order by date ;

create view day_syllabus_total_study_time as
select
    day_study_time.date as date,
    syllabus_name,
    sum(day_study_time.effective_duration) as total_time
from
    (
        select
            DATE(end_time) as date,
            syllabus_name,
            effective_duration
        from session
    ) as day_study_time
group by date, syllabus_name
order by date, syllabus_name ;
