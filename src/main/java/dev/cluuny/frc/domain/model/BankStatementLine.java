package dev.cluuny.frc.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class BankStatementLine {
    private final String referenceId;
    private final BigDecimal amount;
    private final LocalDateTime date;

    public BankStatementLine(String referenceId, BigDecimal amount, LocalDateTime date) {
        this.referenceId = referenceId;
        this.amount = amount;
        this.date = date;
    }
}
