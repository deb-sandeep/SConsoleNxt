package com.sandy.sconsole.dao.session;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table( name = "daily_burn_log" )
public class DailyBurnLog {

    @EmbeddedId
    private DailyBurnLogId id;

    @Column( name = "original_burn_rate", nullable = false )
    private Integer originalBurnRate;

    @Column( name = "current_burn_rate", nullable = false )
    private Integer currentBurnRate;

    @Column( name = "required_burn_rate", nullable = false )
    private Integer requiredBurnRate;

    @Column( name = "today_burn", nullable = false )
    private Integer todayBurn;

    @Column( name = "current_burn_met", nullable = false )
    private Boolean currentBurnMet;

    @Column( name = "original_burn_met", nullable = false )
    private Boolean originalBurnMet;

    @Column( name = "required_burn_met", nullable = false )
    private Boolean requiredBurnMet;

    @Column( name = "required_burn_exceed_pct" )
    private BigDecimal requiredBurnExceedPct;

    @Column( name = "burn_met_override", nullable = false )
    private Boolean burnMetOverride;

    @Column( name = "streak_count", nullable = false )
    private Integer streakCount;
}
