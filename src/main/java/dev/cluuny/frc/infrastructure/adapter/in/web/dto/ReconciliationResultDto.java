package dev.cluuny.frc.infrastructure.adapter.in.web.dto;

import dev.cluuny.frc.domain.model.ReconciliationResult;
import lombok.Data;

@Data
public class ReconciliationResultDto {
    private String transactionReferenceId;
    private String bankReferenceId;
    private String status;

    public static ReconciliationResultDto fromDomain(ReconciliationResult result) {
        ReconciliationResultDto dto = new ReconciliationResultDto();
        if (result.getTransaction() != null) {
            dto.setTransactionReferenceId(result.getTransaction().getReferenceId());
        }
        if (result.getBankStatementLine() != null) {
            dto.setBankReferenceId(result.getBankStatementLine().getReferenceId());
        }
        dto.setStatus(result.getStatus().name());
        return dto;
    }
}
