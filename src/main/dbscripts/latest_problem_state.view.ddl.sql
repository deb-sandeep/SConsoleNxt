CREATE
    ALGORITHM=UNDEFINED
    DEFINER=`root`@`localhost`
    SQL SECURITY DEFINER
    VIEW `latest_problem_state`
AS
select
    `pa`.`problem_id` AS `problem_id`,
    `pa`.`target_state` AS `state`,
    `pa`.`end_time` AS `last_attempt_time`
from (
         select
             `problem_attempt`.`problem_id` AS `problem_id`,
             `problem_attempt`.`target_state` AS `target_state`,
             `problem_attempt`.`end_time` AS `end_time`,
             row_number() OVER (PARTITION BY `problem_attempt`.`problem_id` ORDER BY `problem_attempt`.`start_time` desc )  AS `row_num`
         from
             `problem_attempt`
     ) `pa`
where (
          `pa`.`row_num` = 1
      )
order by
    `pa`.`problem_id`
