package dev.cluuny.frc.domain.service;

import dev.cluuny.frc.domain.model.BankStatementLine;
import dev.cluuny.frc.domain.model.ReconciliationResult;
import dev.cluuny.frc.domain.model.ReconciliationStatus;
import dev.cluuny.frc.domain.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReconciliationMatcherTest {

    private final ReconciliationMatcher matcher = new ReconciliationMatcher(60);

    @Test
    void shouldMatchPerfectly() {
        LocalDateTime now = LocalDateTime.now();
        Transaction t1 = new Transaction("REF1", new BigDecimal("100.00"), now);
        BankStatementLine b1 = new BankStatementLine("REF1", new BigDecimal("100.00"), now);

        List<ReconciliationResult> results = matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.MATCHED, results.get(0).getStatus());
    }

    @Test
    void shouldDetectAmountDiscrepancy() {
        LocalDateTime now = LocalDateTime.now();
        Transaction t1 = new Transaction("REF1", new BigDecimal("100.00"), now);
        BankStatementLine b1 = new BankStatementLine("REF1", new BigDecimal("99.00"), now);

        List<ReconciliationResult> results = matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.DISCREPANCY_AMOUNT, results.get(0).getStatus());
    }

    @Test
    void shouldDetectMissingInBank() {
        LocalDateTime now = LocalDateTime.now();
        Transaction t1 = new Transaction("REF1", new BigDecimal("100.00"), now);

        List<ReconciliationResult> results = matcher.reconcile(List.of(t1), List.of());

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.MISSING_IN_BANK, results.get(0).getStatus());
    }

    @Test
    void shouldDetectMissingInInternal() {
        LocalDateTime now = LocalDateTime.now();
        BankStatementLine b1 = new BankStatementLine("REF1", new BigDecimal("100.00"), now);

        List<ReconciliationResult> results = matcher.reconcile(List.of(), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.MISSING_IN_INTERNAL, results.get(0).getStatus());
    }
    
    @Test
    void shouldMatchWithDateTolerance() {
        LocalDateTime now = LocalDateTime.now();
        Transaction t1 = new Transaction("REF1", new BigDecimal("100.00"), now);
        BankStatementLine b1 = new BankStatementLine("REF1", new BigDecimal("100.00"), now.plusSeconds(30));

        List<ReconciliationResult> results = matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.MATCHED, results.get(0).getStatus());
    }

    @Test
    void shouldMatchEvenIfDateOutsideToleranceIfReferenceMatches() {
        // As per current implementation decision
        LocalDateTime now = LocalDateTime.now();
        Transaction t1 = new Transaction("REF1", new BigDecimal("100.00"), now);
        BankStatementLine b1 = new BankStatementLine("REF1", new BigDecimal("100.00"), now.plusSeconds(120)); // > 60s

        List<ReconciliationResult> results = matcher.reconcile(List.of(t1), List.of(b1));

        assertEquals(1, results.size());
        assertEquals(ReconciliationStatus.MATCHED, results.get(0).getStatus());
    }
}
