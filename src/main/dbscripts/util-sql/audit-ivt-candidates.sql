SELECT id, question_id, problem_type, answer
FROM sconsolenxt.question
WHERE problem_type = 'NVT' AND answer REGEXP '^-?[0-9]+$';
