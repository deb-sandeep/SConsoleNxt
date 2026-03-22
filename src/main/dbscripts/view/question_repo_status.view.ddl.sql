-- sconsolenxt.question_repo_status source

CREATE OR REPLACE
    ALGORITHM = UNDEFINED
    DEFINER = `root`@`localhost`
    SQL SECURITY DEFINER
    VIEW `question_repo_status` AS
select
    `q`.`syllabus_name` AS `syllabus_name`,
    `q`.`topic_id` AS `topic_id`,
    `t`.`topic_name` AS `topic_name`,
    `q`.`problem_type` AS `problem_type`,
    case
        when `eq`.`id` is null then 'UNASSIGNED'
        when `eqa`.`id` is null then 'ASSIGNED'
        else 'ATTEMPTED'
        end AS `question_status`,
    count(`q`.`id`) AS `count`
from
    (((`question` `q`
        left join `topic_master` `t` on
        (`q`.`topic_id` = `t`.`id`))
        left join `exam_question` `eq` on
        (`q`.`id` = `eq`.`question_id`))
        left join `exam_question_attempt` `eqa` on
        (`eq`.`id` = `eqa`.`exam_question_id`))
group by
    `q`.`syllabus_name`,
    `q`.`topic_id`,
    `t`.`topic_name`,
    `q`.`problem_type`,
    case
        when `eq`.`id` is null then 'UNASSIGNED'
        when `eqa`.`id` is null then 'ASSIGNED'
        else 'ATTEMPTED'
        end
order by
    `q`.`syllabus_name`,
    `q`.`topic_id`,
    `q`.`problem_type`,
    case
        when `eq`.`id` is null then 'UNASSIGNED'
        when `eqa`.`id` is null then 'ASSIGNED'
        else 'ATTEMPTED'
        end;
