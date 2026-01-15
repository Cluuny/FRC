package dev.cluuny.frc.application.port.in;

import dev.cluuny.frc.domain.model.BankStatementLine;
import dev.cluuny.frc.domain.model.ReconciliationReport;

import java.util.List;

public interface ReconcileStatementUseCase {
    ReconciliationReport reconcile(List<BankStatementLine> statementLines);
}
