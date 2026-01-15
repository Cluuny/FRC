package dev.cluuny.frc.application.port.out;

import dev.cluuny.frc.domain.model.ReconciliationReport;

public interface ReconciliationReportRepositoryPort {
    void save(ReconciliationReport report);
}
