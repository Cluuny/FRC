package dev.cluuny.rfc.domain.model;

public class ReconciliationResult {
    private Transaction transaction;
    private BankStatementLine bankStatementLine;
    private ReconciliationStatus status;

    public ReconciliationResult(Transaction transaction, BankStatementLine bankStatementLine, ReconciliationStatus status) {
        this.transaction = transaction;
        this.bankStatementLine = bankStatementLine;
        this.status = status;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public BankStatementLine getBankStatementLine() {
        return bankStatementLine;
    }

    public ReconciliationStatus getStatus() {
        return status;
    }
}
