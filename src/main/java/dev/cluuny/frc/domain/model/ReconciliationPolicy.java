package dev.cluuny.frc.domain.model;

import lombok.Getter;

import java.time.Duration;

@Getter
public class ReconciliationPolicy {
    private final Duration exactTolerance;
    private final int maxDayTolerance;
    private final boolean allowAmountFallback;
    private final boolean allowPotentialMatch;

    public ReconciliationPolicy(Duration exactTolerance, int maxDayTolerance, boolean allowAmountFallback, boolean allowPotentialMatch) {
        this.exactTolerance = exactTolerance;
        this.maxDayTolerance = maxDayTolerance;
        this.allowAmountFallback = allowAmountFallback;
        this.allowPotentialMatch = allowPotentialMatch;
    }
}
