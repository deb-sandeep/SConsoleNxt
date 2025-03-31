CREATE
    ALGORITHM=UNDEFINED
    DEFINER=`root`@`localhost`
    SQL SECURITY DEFINER
    VIEW `day_topic_num_solved_problems`
AS
    select
        `day_counts`.`date` AS `date`,
        `day_counts`.`topic_id` AS `topic_id`,
        count(`day_counts`.`problem_id`) AS `num_solved_problems`
    from (
        select
            `pa`.`date` AS `date`,
            `pa`.`topic_id` AS `topic_id`,
            `pa`.`problem_id` AS `problem_id`,
            `pa`.`target_state` AS `target_state`
        from (
            select
                cast(`problem_attempt`.`end_time` as date) AS `date`,
                `problem_attempt`.`topic_id` AS `topic_id`,
                `problem_attempt`.`problem_id` AS `problem_id`,
                `problem_attempt`.`target_state` AS `target_state`,
                row_number() OVER (PARTITION BY `problem_attempt`.`problem_id` ORDER BY `problem_attempt`.`start_time` desc )  AS `row_num`
            from
                `problem_attempt`
        ) `pa`
        where
            (`pa`.`row_num` = 1)
    ) `day_counts`
    where
        (`day_counts`.`target_state` in ('Correct','Incorrect','Pigeon Kill','Purge'))
    group by
        `day_counts`.`date`,
        `day_counts`.`topic_id`
    order by
        `day_counts`.`date` desc
