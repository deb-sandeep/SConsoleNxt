CREATE TABLE sconsolenxt.problem_type_master(
  problem_type VARCHAR(8) NOT NULL,
  PRIMARY KEY(problem_type)
) COMMENT 'Type of problems (MCQ, ART, SCQ, NT, MAT, etc.)' ENGINE = InnoDB
  ROW_FORMAT = Dynamic DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.subject_master(
  subject_name VARCHAR(64) NOT NULL,
  PRIMARY KEY(subject_name)
) COMMENT 'Master catalog of subjects being managed by SConsole' ENGINE = InnoDB
  ROW_FORMAT = Dynamic DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.syllabus_master(
  syllabus_name VARCHAR(64) NOT NULL,
  subject_name VARCHAR(64) NOT NULL,
  PRIMARY KEY(syllabus_name)
) COMMENT '(e.g. IIT-Physics, ISC-Physics, IIT-Chem, etc.)' ENGINE = InnoDB
  ROW_FORMAT = Dynamic DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.topic_master(
  id INT NOT NULL AUTO_INCREMENT,
  syllabus_name VARCHAR(64) NOT NULL,
  section VARCHAR(64) NOT NULL,
  topic_name VARCHAR(256) NOT NULL,
  jee_mains BIT(1) DEFAULT b'0',
  jee_adv BIT(1) DEFAULT b'0',
  PRIMARY KEY(id)
) COMMENT 'First level topics for a syllabus' ENGINE = InnoDB
  ROW_FORMAT = Dynamic AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.subtopic_master(
  id INT NOT NULL AUTO_INCREMENT,
  topic_id INT NOT NULL,
  subtopic_name VARCHAR(256) NOT NULL,
  jee_mains BIT(1) DEFAULT b'0',
  jee_adv BIT(1) DEFAULT b'0',
  PRIMARY KEY(id)
) COMMENT 'Second level topics for a syllabus' ENGINE = InnoDB
  ROW_FORMAT = Dynamic AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.book_master(
  id INT NOT NULL AUTO_INCREMENT,
  subject_name VARCHAR(64) NOT NULL,
  book_name VARCHAR(128) NOT NULL,
  author VARCHAR(128) NOT NULL,
  book_short_name VARCHAR(64),
  acronym VARCHAR(8) NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT `UNIQUE` UNIQUE(acronym)
) COMMENT 'Metadata of individual books' ENGINE = InnoDB ROW_FORMAT = Dynamic
  AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.chapter_master(
  book_id INT NOT NULL,
  chapter_num INT NOT NULL,
  chapter_name INT NOT NULL,
  PRIMARY KEY(
    book_id,
    chapter_num
  ),
  CONSTRAINT `UNIQUE` UNIQUE(
    book_id,
    chapter_num
  )
) COMMENT 'Chapters for a book' ENGINE = InnoDB ROW_FORMAT = Dynamic
  DEFAULT CHARACTER SET = `utf8mb4` COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.problem_master(
  id INT NOT NULL AUTO_INCREMENT,
  book_id INT NOT NULL,
  chapter_num INT NOT NULL,
  exercise_name VARCHAR(64) NOT NULL,
  problem_type VARCHAR(8) NOT NULL,
  problem_id VARCHAR(64) NOT NULL,
  PRIMARY KEY(id)
) COMMENT 'Problems in a chapter' ENGINE = InnoDB ROW_FORMAT = Dynamic
  AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.syllabus_book_map(
  id INT NOT NULL AUTO_INCREMENT,
  syllabus_name VARCHAR(64) NOT NULL,
  book_id INT NOT NULL,
  PRIMARY KEY(id)
) COMMENT 'Map of books applicable for a syllabus' ENGINE = InnoDB
  ROW_FORMAT = Dynamic AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


CREATE TABLE sconsolenxt.topic_chapter_map(
  id INT NOT NULL AUTO_INCREMENT,
  topic_id INT NOT NULL,
  book_id INT NOT NULL,
  chapter_num INT NOT NULL,
  PRIMARY KEY(id)
) COMMENT 'Map of chapters applicable for a syllabus topic' ENGINE = InnoDB
  ROW_FORMAT = Dynamic AUTO_INCREMENT = 1 DEFAULT CHARACTER SET = `utf8mb4`
  COLLATE = `utf8mb4_0900_ai_ci`
;


ALTER TABLE sconsolenxt.chapter_master
  ADD CONSTRAINT fk_book_chapter
    FOREIGN KEY (book_id) REFERENCES sconsolenxt.book_master (id) ON DELETE Restrict
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.syllabus_book_map
  ADD CONSTRAINT `fk_book_syllabus-book-map`
    FOREIGN KEY (book_id) REFERENCES sconsolenxt.book_master (id) ON DELETE Restrict
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.problem_master
  ADD CONSTRAINT fk_chapter_problem
    FOREIGN KEY
      (
        book_id,
        chapter_num
      )
      REFERENCES sconsolenxt.chapter_master
        (
          book_id,
          chapter_num
        ) ON DELETE Restrict ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.topic_chapter_map
  ADD CONSTRAINT `fk_chapter_topic-chapter-map`
    FOREIGN KEY
      (
        book_id,
        chapter_num
      )
      REFERENCES sconsolenxt.chapter_master
        (
          book_id,
          chapter_num
        ) ON DELETE Restrict ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.problem_master
  ADD CONSTRAINT `fk_problem-type_problem`
    FOREIGN KEY (problem_type)
      REFERENCES sconsolenxt.problem_type_master (problem_type) ON DELETE Restrict
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.book_master
  ADD CONSTRAINT fk_subject_book
    FOREIGN KEY (subject_name) REFERENCES sconsolenxt.subject_master (subject_name)
      ON DELETE Restrict ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.syllabus_master
  ADD CONSTRAINT fk_subject_syllabus
    FOREIGN KEY (subject_name) REFERENCES sconsolenxt.subject_master (subject_name)
      ON DELETE Restrict ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.syllabus_book_map
  ADD CONSTRAINT `fk_syllabus_syllabus-book-map`
    FOREIGN KEY (syllabus_name)
      REFERENCES sconsolenxt.syllabus_master (syllabus_name) ON DELETE Restrict
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.topic_master
  ADD CONSTRAINT fk_syllabus_topic
    FOREIGN KEY (syllabus_name)
      REFERENCES sconsolenxt.syllabus_master (syllabus_name) ON DELETE Restrict
      ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.subtopic_master
  ADD CONSTRAINT fk_topic_subtopic
    FOREIGN KEY (topic_id) REFERENCES sconsolenxt.topic_master (id)
      ON DELETE Restrict ON UPDATE Cascade
;


ALTER TABLE sconsolenxt.topic_chapter_map
  ADD CONSTRAINT `fk_topic_topic-chapter-map`
    FOREIGN KEY (topic_id) REFERENCES sconsolenxt.topic_master (id)
      ON DELETE Restrict ON UPDATE Cascade
;

