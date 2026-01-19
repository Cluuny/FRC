package dev.cluuny.frc.infrastructure.adapter.out.persistence.repository;

import dev.cluuny.frc.infrastructure.adapter.out.persistence.entity.ReconciliationReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaReconciliationReportRepository extends JpaRepository<ReconciliationReportEntity, String> {
    Optional<ReconciliationReportEntity> findBySignature(String signature);
}
