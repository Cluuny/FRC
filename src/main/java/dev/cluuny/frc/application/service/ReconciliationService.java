package dev.cluuny.frc.application.service;

import dev.cluuny.frc.application.port.in.ReconcileStatementUseCase;
import dev.cluuny.frc.application.port.out.ReconciliationReportRepositoryPort;
import dev.cluuny.frc.application.port.out.TransactionRepositoryPort;
import dev.cluuny.frc.domain.exception.EmptyStatementException;
import dev.cluuny.frc.domain.exception.ReportStorageException;
import dev.cluuny.frc.domain.model.*;
import dev.cluuny.frc.domain.service.ReconciliationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReconciliationService implements ReconcileStatementUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationService.class);

    private final TransactionRepositoryPort transactionRepository;
    private final ReconciliationReportRepositoryPort reportRepository;
    private final ReconciliationMatcher matcher;

    public ReconciliationService(ReconciliationPolicy policy, TransactionRepositoryPort transactionRepository,
                                 ReconciliationReportRepositoryPort reportRepository) {
        this.transactionRepository = transactionRepository;
        this.reportRepository = reportRepository;
        this.matcher = new ReconciliationMatcher(policy);
    }

    @Override
    @Transactional
    public ReconciliationReport reconcile(List<BankStatementLine> statementLines) {
        if (statementLines == null || statementLines.isEmpty()) {
            throw new EmptyStatementException("Bank statement lines cannot be null or empty");
        }

        logger.info("Starting reconciliation for {} statement lines", statementLines.size());
        
        List<Transaction> internalTransactions = transactionRepository.findAll();
        logger.debug("Fetched {} internal transactions", internalTransactions.size());
        
        List<ReconciliationResult> results = matcher.reconcile(internalTransactions, statementLines);
        logger.info("Reconciliation completed with {} results", results.size());
        
        ReconciliationReport report = new ReconciliationReport(results);
        try {
            reportRepository.save(report);
        } catch (Exception e) {
            throw new ReportStorageException("Failed to save reconciliation report", e);
        }
        logger.info("Reconciliation report saved");

        return report;
    }
}
