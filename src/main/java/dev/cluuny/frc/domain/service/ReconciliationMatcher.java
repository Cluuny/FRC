package dev.cluuny.frc.domain.service;

import dev.cluuny.frc.domain.exception.InvalidTransactionDataException;
import dev.cluuny.frc.domain.model.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReconciliationMatcher {

    private final ReconciliationPolicy policy;

    public ReconciliationMatcher(ReconciliationPolicy policy) {
        this.policy = policy;
    }

    private long secondsDiff(Transaction t, BankStatementLine b) {
        if (t.getDate() == null || b.getDate() == null) {
            throw new InvalidTransactionDataException("Transaction or Bank Statement Line date cannot be null");
        }
        return Math.abs(Duration.between(t.getDate(), b.getDate()).getSeconds());
    }

    private boolean withinSecondsTolerance(Transaction t, BankStatementLine b) {
        return secondsDiff(t, b) <= policy.getExactTolerance().getSeconds();
    }

    private boolean withinDaysTolerance(Transaction t, BankStatementLine b) {
        long days = secondsDiff(t, b) / (24 * 60 * 60);
        return days <= policy.getMaxDayTolerance();
    }

    private List<Transaction> findByReference(
            List<Transaction> internals,
            BankStatementLine bank) {

        if (bank.getReferenceId() == null) {
            return List.of(); // Or throw an exception depending on business rule
        }

        return internals.stream()
                .filter(t -> t.getReferenceId() != null && t.getReferenceId().equals(bank.getReferenceId()))
                .toList();
    }

    private List<Transaction> findByAmount(
            List<Transaction> internals,
            BankStatementLine bank) {
        
        if (bank.getAmount() == null) {
             throw new InvalidTransactionDataException("Bank Statement Line amount cannot be null");
        }

        return internals.stream()
                .filter(t -> {
                    if (t.getAmount() == null) {
                        throw new InvalidTransactionDataException("Internal Transaction amount cannot be null for transaction ID: " + t.getReferenceId());
                    }
                    return t.getAmount().compareTo(bank.getAmount()) == 0;
                })
                .toList();
    }

    private Optional<ReconciliationResult> tryReferenceMatch(
            Transaction internal,
            BankStatementLine bank) {

        if (internal.getAmount() == null || bank.getAmount() == null) {
             throw new InvalidTransactionDataException("Amounts cannot be null for comparison");
        }

        if (internal.getAmount().compareTo(bank.getAmount()) != 0) {
            return Optional.of(new ReconciliationResult(
                    internal, bank, ReconciliationStatus.AMOUNT_MISMATCH));
        }

        if (withinSecondsTolerance(internal, bank)) {
            return Optional.of(new ReconciliationResult(
                    internal, bank, ReconciliationStatus.RECONCILED_EXACT));
        }

        if (withinDaysTolerance(internal, bank)) {
            return Optional.of(new ReconciliationResult(
                    internal, bank, ReconciliationStatus.RECONCILED_TIME_TOLERANCE));
        }

        return Optional.of(new ReconciliationResult(
                internal, bank, ReconciliationStatus.DATE_MISMATCH));
    }

    private Optional<ReconciliationResult> tryAmountFallback(
            Transaction internal,
            BankStatementLine bank) {

        if (withinSecondsTolerance(internal, bank)) {
            return Optional.of(new ReconciliationResult(
                    internal, bank, ReconciliationStatus.POTENTIAL_MATCH));
        }

        return Optional.empty();
    }

    /**
     *
     */
    public List<ReconciliationResult> reconcile(
            List<Transaction> internalTransactions,
            List<BankStatementLine> bankStatementLines) {

        // Se verifica que la lista bancaria no sea nula o esté vacía
        if (internalTransactions == null || internalTransactions.isEmpty()) {
             throw new InvalidTransactionDataException("Internal transactions list cannot be null or empty");
        }

        List<ReconciliationResult> results = new ArrayList<>();

        // Copias manipulables
        List<Transaction> unmatchedInternal = new ArrayList<>(internalTransactions);
        List<BankStatementLine> unmatchedBank = new ArrayList<>(bankStatementLines);

        // Reconcilia por referenceId - Luego por Amount
        // Usamos una copia para iterar porque modificaremos unmatchedBank dentro del bucle si es necesario,
        // aunque en este enfoque iteramos sobre la lista original y eliminamos de las listas 'unmatched'.
        for (BankStatementLine bankLine : bankStatementLines) {

            List<Transaction> refMatches =
                    findByReference(unmatchedInternal, bankLine);

            if (!refMatches.isEmpty()) {

                // Si existen varios candidatos se pasa por AMBIGUOUS_MATCH
                if (refMatches.size() > 1) {
                    results.add(new ReconciliationResult(
                            null, bankLine, ReconciliationStatus.AMBIGUOUS_MATCH));
                    // Se marca como procesado en el banco para no reportarlo como missing
                    unmatchedInternal.removeAll(refMatches);
                    unmatchedBank.remove(bankLine);
                    continue;
                }

                // Intenta realizar el match por referenceId
                Transaction internal = refMatches.getFirst();
                Optional<ReconciliationResult> result =
                        tryReferenceMatch(internal, bankLine);

                if (result.isPresent()) {
                    results.add(result.get());
                    // Si hubo match por referencia (incluso con mismatch de monto o fecha),
                    // se consideran ambas partes como "procesadas" para este matcher.
                    unmatchedInternal.remove(internal);
                    unmatchedBank.remove(bankLine);
                }
                continue;
            }

            // Obtiene la lista de coincidencias por amount
            List<Transaction> amountMatches =
                    findByAmount(unmatchedInternal, bankLine);

            // Si existen varios candidatos se pasa por AMBIGUOUS_MATCH
            if (amountMatches.size() > 1) {
                results.add(new ReconciliationResult(
                        null, bankLine, ReconciliationStatus.AMBIGUOUS_MATCH));
                unmatchedInternal.removeAll(amountMatches);
                unmatchedBank.remove(bankLine);
                continue;
            }

            // Se verifica por amount
            if (amountMatches.size() == 1) {
                Transaction internal = amountMatches.getFirst();
                Optional<ReconciliationResult> fallback =
                        tryAmountFallback(internal, bankLine);

                if (fallback.isPresent()) {
                    results.add(fallback.get());
                    // Si es POTENTIAL_MATCH, decidimos si quitarlo o no.
                    unmatchedInternal.remove(internal);
                    unmatchedBank.remove(bankLine);
                }
            }
            
            // Si se llega a este punto, no hubo match por referencia ni por monto (o el fallback falló).
            // No se agrega UNMATCHED de forma explicita para agregar MISSING_IN_BANK o MISSING_IN_INTERNAL
            // Y de esta forma que se pase a una tentativa revision manual
        }

        for (Transaction internal : unmatchedInternal) {
            results.add(new ReconciliationResult(
                    internal, null, ReconciliationStatus.MISSING_IN_BANK));
        }

        for (BankStatementLine bank : unmatchedBank) {
            results.add(new ReconciliationResult(
                    null, bank, ReconciliationStatus.MISSING_IN_INTERNAL));
        }

        return results;
    }
}
