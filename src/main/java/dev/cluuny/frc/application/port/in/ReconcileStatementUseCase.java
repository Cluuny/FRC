package dev.cluuny.rfc.application.port.in;

import dev.cluuny.rfc.domain.model.BankStatementLine;
import dev.cluuny.rfc.domain.model.ReconciliationReport;

import java.util.List;

public interface ReconcileStatementUseCase {
    ReconciliationReport reconcile(List<BankStatementLine> statementLines);
}
