package dev.cluuny.frc.domain.service;

import dev.cluuny.frc.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReconciliationMatcherTest {
    private final ReconciliationMatcher matcher =
            new ReconciliationMatcher(new ReconciliationPolicy(Duration.ofSeconds(60), 2, true, true));

    @Test
    void shouldReconcileExactMatch() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 =
                new Transaction("REF1", new BigDecimal("100.00"), now);
        BankStatementLine b1 =
                new BankStatementLine("REF1", new BigDecimal("100.00"), now);

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(
                ReconciliationStatus.RECONCILED_EXACT,
                results.getFirst().getStatus()
        );
    }

    @Test
    void shouldDetectAmountMismatch() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 =
                new Transaction("REF1", new BigDecimal("100.00"), now);
        BankStatementLine b1 =
                new BankStatementLine("REF1", new BigDecimal("99.00"), now);

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(
                ReconciliationStatus.AMOUNT_MISMATCH,
                results.getFirst().getStatus()
        );
    }

    @Test
    void shouldDetectMissingInBank() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 =
                new Transaction("REF1", new BigDecimal("100.00"), now);

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1), List.of());

        assertEquals(1, results.size());
        assertEquals(
                ReconciliationStatus.MISSING_IN_BANK,
                results.getFirst().getStatus()
        );
    }

    @Test
    void shouldDetectMissingInInternal() {
        LocalDateTime now = LocalDateTime.now();

        BankStatementLine b1 =
                new BankStatementLine("REF1", new BigDecimal("100.00"), now);

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(
                ReconciliationStatus.MISSING_IN_INTERNAL,
                results.getFirst().getStatus()
        );
    }

    @Test
    void shouldReconcileWithSecondsTolerance() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 =
                new Transaction("REF1", new BigDecimal("100.00"), now);
        BankStatementLine b1 =
                new BankStatementLine(
                        "REF1",
                        new BigDecimal("100.00"),
                        now.plusSeconds(30)
                );

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(
                ReconciliationStatus.RECONCILED_EXACT,
                results.getFirst().getStatus()
        );
    }

    @Test
    void shouldReconcileWithDayTolerance() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 =
                new Transaction("REF1", new BigDecimal("100.00"), now);
        BankStatementLine b1 =
                new BankStatementLine(
                        "REF1",
                        new BigDecimal("100.00"),
                        now.plusDays(1)
                );

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(
                ReconciliationStatus.RECONCILED_TIME_TOLERANCE,
                results.getFirst().getStatus()
        );
    }

    @Test
    void shouldDetectAmbiguousReferenceMatch() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 = new Transaction("REF1", new BigDecimal("100"), now);
        Transaction t2 = new Transaction("REF1", new BigDecimal("100"), now);

        BankStatementLine b1 = new BankStatementLine("REF1", new BigDecimal("100"), now);

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1, t2), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.AMBIGUOUS_MATCH, results.getFirst().getStatus());
    }

    @Test
    void shouldDetectAmbiguousAmountMatch() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 = new Transaction("A", new BigDecimal("100"), now);
        Transaction t2 = new Transaction("B", new BigDecimal("100"), now);

        BankStatementLine b1 = new BankStatementLine("X", new BigDecimal("100"), now);

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1, t2), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.AMBIGUOUS_MATCH, results.getFirst().getStatus());
    }

    @Test
    void shouldDetectDateMismatch() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 = new Transaction("REF1", new BigDecimal("100"), now);
        BankStatementLine b1 = new BankStatementLine(
                "REF1",
                new BigDecimal("100"),
                now.plusDays(10)
        );

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.DATE_MISMATCH, results.getFirst().getStatus());
    }

    @Test
    void shouldProducePotentialMatch() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 = new Transaction("INT1", new BigDecimal("100"), now);
        BankStatementLine b1 = new BankStatementLine(
                "BANK1",
                new BigDecimal("100"),
                now.plusSeconds(30)
        );

        List<ReconciliationResult> results =
                matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.POTENTIAL_MATCH, results.getFirst().getStatus());
    }

}
