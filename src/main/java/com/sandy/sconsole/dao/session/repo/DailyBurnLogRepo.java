package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.DailyBurnLog;
import com.sandy.sconsole.dao.session.DailyBurnLogId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyBurnLogRepo extends JpaRepository<DailyBurnLog, DailyBurnLogId> {
}
