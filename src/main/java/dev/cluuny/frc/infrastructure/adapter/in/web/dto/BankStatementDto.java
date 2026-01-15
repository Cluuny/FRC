package dev.cluuny.frc.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BankStatementDto {
    @Schema(description = "Unique reference ID of the transaction from the bank", example = "REF-123456")
    private String referenceId;
    
    @Schema(description = "Transaction amount", example = "150.50")
    private BigDecimal amount;
    
    @Schema(description = "Date and time of the transaction", example = "2023-10-27T10:00:00")
    private LocalDateTime date;
}
