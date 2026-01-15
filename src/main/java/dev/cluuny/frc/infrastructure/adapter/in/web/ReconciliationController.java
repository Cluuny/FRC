package dev.cluuny.frc.infrastructure.adapter.in.web;

import dev.cluuny.frc.application.port.in.ReconcileStatementUseCase;
import dev.cluuny.frc.domain.model.BankStatementLine;
import dev.cluuny.frc.domain.model.ReconciliationReport;
import dev.cluuny.frc.infrastructure.adapter.in.web.dto.BankStatementDto;
import dev.cluuny.frc.infrastructure.adapter.in.web.dto.ReconciliationReportDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Tag(name = "Reconciliation", description = "API for reconciling bank statements with internal records")
public class ReconciliationController {

    private final ReconcileStatementUseCase reconcileStatementUseCase;

    public ReconciliationController(ReconcileStatementUseCase reconcileStatementUseCase) {
        this.reconcileStatementUseCase = reconcileStatementUseCase;
    }

    @Operation(summary = "Process a bank statement", description = "Reconciles a list of bank statement lines against internal transactions.")
    @ApiResponse(responseCode = "200", description = "Reconciliation report generated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReconciliationReportDto.class)))
    @PostMapping("/process")
    public ResponseEntity<ReconciliationReportDto> processStatement(
            @Parameter(description = "Idempotency key to prevent duplicate processing", required = false)
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody List<BankStatementDto> statementDtos) {
        
        List<BankStatementLine> lines = statementDtos.stream()
                .map(dto -> new BankStatementLine(dto.getReferenceId(), dto.getAmount(), dto.getDate()))
                .collect(Collectors.toList());

        ReconciliationReport report = reconcileStatementUseCase.reconcile(lines);

        ReconciliationReportDto responseDto = ReconciliationReportDto.fromDomain(report);
        
        return ResponseEntity.ok(responseDto);
    }
}
