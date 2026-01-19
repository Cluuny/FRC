package dev.cluuny.frc.application.port.out;

import dev.cluuny.frc.domain.model.ReconciliationReport;

import java.util.Optional;

public interface ReconciliationReportRepositoryPort {
    void save(ReconciliationReport report, String signature);
    Optional<ReconciliationReport> findBySignature(String signature);
}
