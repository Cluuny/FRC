package dev.cluuny.frc.domain.model;

import lombok.Getter;

@Getter
public class ReconciliationResult {
    private final Transaction transaction;
    private final BankStatementLine bankStatementLine;
    private final ReconciliationStatus status;

    public ReconciliationResult(Transaction transaction, BankStatementLine bankStatementLine, ReconciliationStatus status) {
        this.transaction = transaction;
        this.bankStatementLine = bankStatementLine;
        this.status = status;
    }
}
