package dev.cluuny.frc.infrastructure.adapter.out.persistence.repository;

import dev.cluuny.frc.infrastructure.adapter.out.persistence.entity.ReconciliationReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReconciliationReportRepository extends JpaRepository<ReconciliationReportEntity, String> {
}
