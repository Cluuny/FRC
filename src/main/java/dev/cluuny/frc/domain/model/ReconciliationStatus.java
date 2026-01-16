package dev.cluuny.frc.domain.model;

public enum ReconciliationStatus {
    MATCHED,
    DISCREPANCY_AMOUNT,
    MISSING_IN_BANK,
    MISSING_IN_INTERNAL,
    RECONCILED_EXACT,
    RECONCILED_TIME_TOLERANCE,
    POTENTIAL_MATCH,
    UNMATCHED,
    DUPLICATE,
    AMOUNT_MISMATCH,
    DATE_MISMATCH,
    AMBIGUOUS_MATCH
}
