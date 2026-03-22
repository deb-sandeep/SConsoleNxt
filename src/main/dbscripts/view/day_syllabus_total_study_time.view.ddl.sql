CREATE
    ALGORITHM = UNDEFINED
    DEFINER = `root`@`localhost`
    SQL SECURITY DEFINER
    VIEW `day_syllabus_total_study_time` AS
SELECT
    `day_study_time`.`date` AS `date`,
    `day_study_time`.`syllabus_name` AS `syllabus_name`,
    SUM(`day_study_time`.`effective_duration`) AS `total_time`
FROM
    (SELECT
         CAST(`session`.`end_time` AS DATE) AS `date`,
         `session`.`syllabus_name` AS `syllabus_name`,
         `session`.`effective_duration` AS `effective_duration`
     FROM
         `session`) `day_study_time`
GROUP BY `day_study_time`.`date` , `day_study_time`.`syllabus_name`
ORDER BY `day_study_time`.`date` , `day_study_time`.`syllabus_name`
