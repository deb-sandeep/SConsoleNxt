CREATE
    ALGORITHM = UNDEFINED
    DEFINER = `root`@`localhost`
    SQL SECURITY DEFINER
    VIEW `topic_problems` AS
SELECT
    `tcm`.`topic_id` AS `topic_id`,
    `t`.`syllabus_name` AS `syllabus_name`,
    `t`.`section` AS `topic_section`,
    `t`.`topic_name` AS `topic_name`,
    `p`.`book_id` AS `book_id`,
    `b`.`book_short_name` AS `book_short_name`,
    `b`.`series_name` AS `book_series`,
    `p`.`chapter_num` AS `chapter_num`,
    `c`.`chapter_name` AS `chapter_name`,
    `p`.`id` AS `problem_id`,
    `p`.`exercise_num` AS `exercise_num`,
    `p`.`exercise_name` AS `exercise_name`,
    `p`.`problem_type` AS `problem_type`,
    `p`.`problem_key` AS `problem_key`,
    `p`.`difficulty_level` AS `difficulty_level`,
    (CASE
         WHEN (`ps`.`state` IS NULL) THEN 'Assigned'
         ELSE `ps`.`state`
        END) AS `problem_state`,
    `ps`.`last_attempt_time` AS `last_attempt_time`,
    (CASE
         WHEN (`ps`.`total_duration` IS NULL) THEN 0
         ELSE `ps`.`total_duration`
        END) AS `total_duration`,
    (CASE
         WHEN (`ps`.`num_attempts` IS NULL) THEN 0
         ELSE `ps`.`num_attempts`
        END) AS `num_attempts`
FROM
    ((((((`problem_master` `p`
        LEFT JOIN `topic_chapter_problem_map` `tcpm` ON ((`p`.`id` = `tcpm`.`problem_id`)))
        LEFT JOIN `topic_chapter_map` `tcm` ON ((`tcpm`.`topic_chapter_map_id` = `tcm`.`id`)))
        LEFT JOIN `topic_master` `t` ON ((`tcm`.`topic_id` = `t`.`id`)))
        LEFT JOIN `book_master` `b` ON ((`tcm`.`book_id` = `b`.`id`)))
        LEFT JOIN `chapter_master` `c` ON (((`tcm`.`book_id` = `c`.`book_id`)
        AND (`tcm`.`chapter_num` = `c`.`chapter_num`))))
        LEFT JOIN `latest_problem_state` `ps` ON ((`p`.`id` = `ps`.`problem_id`)))
WHERE
    (`tcm`.`topic_id` IS NOT NULL)
ORDER BY `tcm`.`topic_id` , `tcm`.`attempt_seq` , `p`.`id`
