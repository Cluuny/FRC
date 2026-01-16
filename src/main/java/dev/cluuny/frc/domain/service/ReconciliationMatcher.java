package dev.cluuny.frc.domain.service;

import dev.cluuny.frc.domain.model.BankStatementLine;
import dev.cluuny.frc.domain.model.ReconciliationResult;
import dev.cluuny.frc.domain.model.ReconciliationStatus;
import dev.cluuny.frc.domain.model.Transaction;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReconciliationMatcher {

    private final long dateToleranceSeconds;
    private final int dateToleranceDays;

    public ReconciliationMatcher(long dateToleranceSeconds, int dateToleranceDays) {
        this.dateToleranceSeconds = dateToleranceSeconds;
        this.dateToleranceDays = dateToleranceDays;
    }

    private long secondsDiff(Transaction t, BankStatementLine b) {
        return Math.abs(Duration.between(t.getDate(), b.getDate()).getSeconds());
    }

    private boolean withinSecondsTolerance(Transaction t, BankStatementLine b) {
        return secondsDiff(t, b) <= dateToleranceSeconds;
    }

    private boolean withinDaysTolerance(Transaction t, BankStatementLine b) {
        long days = secondsDiff(t, b) / (24 * 60 * 60);
        return days <= dateToleranceDays;
    }

    private List<Transaction> findByReference(
            List<Transaction> internals,
            BankStatementLine bank) {

        return internals.stream()
                .filter(t -> t.getReferenceId().equals(bank.getReferenceId()))
                .toList();
    }

    private List<Transaction> findByAmount(
            List<Transaction> internals,
            BankStatementLine bank) {

        return internals.stream()
                .filter(t -> t.getAmount().compareTo(bank.getAmount()) == 0)
                .toList();
    }

    private Optional<ReconciliationResult> tryReferenceMatch(
            Transaction internal,
            BankStatementLine bank) {

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
     * Reconcilia transacciones internas con registros del Banco
     */
    public List<ReconciliationResult> reconcile(
            List<Transaction> internalTransactions,
            List<BankStatementLine> bankStatementLines) {

        List<ReconciliationResult> results = new ArrayList<>();

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
                    // Marcamos como procesado en el banco para no reportarlo como missing
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
                    // consideramos ambas partes como "procesadas" para este matcher.
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
                    // Generalmente si ya se reportó como match potencial, no queremos que salga como missing.
                    unmatchedInternal.remove(internal);
                    unmatchedBank.remove(bankLine);
                    continue;
                }
            }
            
            // Si llegamos aquí, no hubo match por referencia ni por monto (o el fallback falló).
            // NO agregamos UNMATCHED aquí explícitamente si queremos que se reporte como MISSING_IN_INTERNAL al final.
            // O bien, si agregamos UNMATCHED, debemos quitarlo de unmatchedBank.
            // La lógica original agregaba UNMATCHED y luego MISSING_IN_INTERNAL.
            // Para cumplir con el test shouldDetectMissingInInternal (que espera 1 solo resultado),
            // dejaremos que el bucle final se encargue de reportarlo como MISSING_IN_INTERNAL.
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
