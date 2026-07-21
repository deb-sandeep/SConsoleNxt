-- Run audit-ivt-candidates.sql first and manually review the result set.
-- Populate the id list below with only the reviewed/approved rows before running.
UPDATE sconsolenxt.question
SET problem_type = 'IVT'
WHERE problem_type = 'NVT'
  AND id IN ( /* reviewed ids go here */ );
