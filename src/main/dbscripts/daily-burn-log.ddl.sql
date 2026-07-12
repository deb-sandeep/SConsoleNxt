CREATE TABLE sconsolenxt.daily_burn_log(
  date DATE NOT NULL,
  topic_id INT NOT NULL,
  original_burn_rate INT NOT NULL,
  current_burn_rate INT NOT NULL,
  required_burn_rate INT NOT NULL,
  today_burn INT NOT NULL,
  current_burn_met TINYINT(1) NOT NULL,
  original_burn_met TINYINT(1) NOT NULL,
  required_burn_met TINYINT(1) NOT NULL,
  required_burn_exceed_pct DECIMAL(6,2),
  streak_count INT NOT NULL DEFAULT 0,
  PRIMARY KEY(date, topic_id)
) COMMENT 'Live per-topic daily burn snapshot, updated as problems are attempted; drives the full-burn streak feature'
  ENGINE = InnoDB ROW_FORMAT = Dynamic DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_unicode_ci`
;


ALTER TABLE sconsolenxt.daily_burn_log
  ADD CONSTRAINT fk_topic_daily_burn_log
    FOREIGN KEY (topic_id) REFERENCES sconsolenxt.topic_master (id) ON DELETE Restrict
      ON UPDATE Cascade
;
