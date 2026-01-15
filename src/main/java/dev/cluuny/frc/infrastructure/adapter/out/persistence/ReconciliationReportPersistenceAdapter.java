package dev.cluuny.frc.infrastructure.adapter.out.persistence;

import dev.cluuny.frc.application.port.out.ReconciliationReportRepositoryPort;
import dev.cluuny.frc.domain.model.ReconciliationReport;
import dev.cluuny.frc.domain.model.ReconciliationResult;
import dev.cluuny.frc.infrastructure.adapter.out.persistence.entity.ReconciliationReportEntity;
import dev.cluuny.frc.infrastructure.adapter.out.persistence.entity.ReconciliationResultEntity;
import dev.cluuny.frc.infrastructure.adapter.out.persistence.repository.JpaReconciliationReportRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReconciliationReportPersistenceAdapter implements ReconciliationReportRepositoryPort {

    private final JpaReconciliationReportRepository repository;

    public ReconciliationReportPersistenceAdapter(JpaReconciliationReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(ReconciliationReport report) {
        String username = "SYSTEM";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }

        ReconciliationReportEntity entity = new ReconciliationReportEntity(username);
        List<ReconciliationResultEntity> resultEntities = new ArrayList<>();

        for (ReconciliationResult result : report.getResults()) {
            ReconciliationResultEntity resultEntity = new ReconciliationResultEntity();
            resultEntity.setReport(entity);
            resultEntity.setStatus(result.getStatus());

            if (result.getTransaction() != null) {
                resultEntity.setTransactionReferenceId(result.getTransaction().getReferenceId());
                resultEntity.setTransactionAmount(result.getTransaction().getAmount());
                resultEntity.setTransactionDate(result.getTransaction().getDate());
            }

            if (result.getBankStatementLine() != null) {
                resultEntity.setBankReferenceId(result.getBankStatementLine().getReferenceId());
                resultEntity.setBankAmount(result.getBankStatementLine().getAmount());
                resultEntity.setBankDate(result.getBankStatementLine().getDate());
            }
            resultEntities.add(resultEntity);
        }

        entity.setResults(resultEntities);
        repository.save(entity);
    }
}
