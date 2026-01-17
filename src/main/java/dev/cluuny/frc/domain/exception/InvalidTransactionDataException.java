package dev.cluuny.frc.domain.exception;

public class InvalidTransactionDataException extends ReconciliationException {
    public InvalidTransactionDataException(String message) {
        super(message);
    }
}
