package dev.cluuny.frc.domain.model;

public enum ReconciliationStatus {
    MATCHED,
    MISSING_IN_BANK,
    MISSING_IN_INTERNAL,
    RECONCILED_EXACT,
    RECONCILED_TIME_TOLERANCE,
    POTENTIAL_MATCH,
    AMOUNT_MISMATCH,
    DATE_MISMATCH,
    AMBIGUOUS_MATCH
}
