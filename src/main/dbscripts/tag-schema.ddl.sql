CREATE TABLE sconsolenxt.tag_master(
  id INT NOT NULL AUTO_INCREMENT,
  tag_text VARCHAR(128) NOT NULL,
  normalized_tag_text VARCHAR(128) NOT NULL,
  topic_id INT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY(id)
) COMMENT 'Global catalog of free-format concept tags. Unique by normalized text; each tag has one dominant/home topic chosen at creation but may be attached to problems/questions in any topic or subject.'
  ENGINE = InnoDB ROW_FORMAT = Dynamic AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_unicode_ci`
;


CREATE TABLE sconsolenxt.tag_problem_map(
  id INT NOT NULL AUTO_INCREMENT,
  problem_id INT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY(id)
) COMMENT 'Association of tags to practice problems (problem_master). A tag may be applied to any problem regardless of its home topic.'
  ENGINE = InnoDB ROW_FORMAT = Dynamic AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_unicode_ci`
;


CREATE TABLE sconsolenxt.tag_question_map(
  id INT NOT NULL AUTO_INCREMENT,
  question_id INT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY(id)
) COMMENT 'Association of tags to CBT exam bank questions (question). A tag may be applied to any question regardless of its home topic.'
  ENGINE = InnoDB ROW_FORMAT = Dynamic AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_unicode_ci`
;


ALTER TABLE sconsolenxt.tag_master
  ADD CONSTRAINT uq_tag_master_normalized_tag_text UNIQUE(normalized_tag_text)
;


ALTER TABLE sconsolenxt.tag_master
  ADD CONSTRAINT fk_tag_master_topic_master
    FOREIGN KEY (topic_id) REFERENCES sconsolenxt.topic_master (id) ON DELETE Restrict
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.tag_problem_map
  ADD CONSTRAINT uq_tag_problem_map_problem_id_tag_id UNIQUE(problem_id, tag_id)
;


ALTER TABLE sconsolenxt.tag_problem_map
  ADD CONSTRAINT fk_tag_problem_map_problem_master
    FOREIGN KEY (problem_id) REFERENCES sconsolenxt.problem_master (id) ON DELETE Cascade
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.tag_problem_map
  ADD CONSTRAINT fk_tag_problem_map_tag_master
    FOREIGN KEY (tag_id) REFERENCES sconsolenxt.tag_master (id) ON DELETE Cascade
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.tag_question_map
  ADD CONSTRAINT uq_tag_question_map_question_id_tag_id UNIQUE(question_id, tag_id)
;


ALTER TABLE sconsolenxt.tag_question_map
  ADD CONSTRAINT fk_tag_question_map_question
    FOREIGN KEY (question_id) REFERENCES sconsolenxt.question (id) ON DELETE Cascade
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.tag_question_map
  ADD CONSTRAINT fk_tag_question_map_tag_master
    FOREIGN KEY (tag_id) REFERENCES sconsolenxt.tag_master (id) ON DELETE Cascade
      ON UPDATE Cascade
;


CREATE TABLE sconsolenxt.tag_recent_usage(
  tag_id INT NOT NULL,
  last_used_at DATETIME NOT NULL,
  PRIMARY KEY(tag_id)
) COMMENT 'Rolling cache of the most recently used tags (capped at 30 rows by application code), powering a quick-pick recently-used-tags list.'
  ENGINE = InnoDB ROW_FORMAT = Dynamic DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_unicode_ci`
;


ALTER TABLE sconsolenxt.tag_recent_usage
  ADD CONSTRAINT fk_tag_recent_usage_tag_master
    FOREIGN KEY (tag_id) REFERENCES sconsolenxt.tag_master (id) ON DELETE Cascade
      ON UPDATE Cascade
;


CREATE TABLE sconsolenxt.saved_tag_query(
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  query LONGTEXT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY(id)
) COMMENT 'User-saved tag-browser search criteria (boolean tag expression + filters), stored as serialized JSON for later recall. Saving under an existing name overwrites that row (name is the upsert key).'
  ENGINE = InnoDB ROW_FORMAT = Dynamic AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_unicode_ci`
;


ALTER TABLE sconsolenxt.saved_tag_query
  ADD CONSTRAINT uq_saved_tag_query_name UNIQUE(name)
;
