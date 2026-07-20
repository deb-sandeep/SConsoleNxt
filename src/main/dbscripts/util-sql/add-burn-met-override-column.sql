ALTER TABLE sconsolenxt.daily_burn_log
  ADD COLUMN burn_met_override TINYINT(1) NOT NULL DEFAULT 0 AFTER required_burn_exceed_pct
;
