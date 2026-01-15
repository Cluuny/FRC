package dev.cluuny.rfc.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private String referenceId;
    private BigDecimal amount;
    private LocalDateTime date;

    public Transaction(String referenceId, BigDecimal amount, LocalDateTime date) {
        this.referenceId = referenceId;
        this.amount = amount;
        this.date = date;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
