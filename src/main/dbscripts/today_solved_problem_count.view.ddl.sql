create view today_solved_problem_count as
select
    pa.topic_id,
    count( pa.problem_id )
from
    (
        select
            problem_id,
            topic_id,
            target_state,
            row_number() OVER ( PARTITION BY problem_id ORDER BY end_time desc ) AS row_num
        from
            problem_attempt
        where
            end_time between CURDATE() AND DATE_ADD( CURDATE(), INTERVAL 1 DAY )
    ) pa
where
    pa.row_num = 1 AND
    pa.target_state in ( 'Correct', 'Incorrect' )
group by
    pa.topic_id
order by
    pa.topic_id ;
