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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        String signature = generateSignature(statementLines);
        Optional<ReconciliationReport> existingReport = reportRepository.findBySignature(signature);
        if (existingReport.isPresent()) {
            logger.info("Returning existing reconciliation report for signature: {}", signature);
            return existingReport.get();
        }

        logger.info("Starting reconciliation for {} statement lines", statementLines.size());
        
        List<Transaction> internalTransactions = transactionRepository.findAll();
        logger.debug("Fetched {} internal transactions", internalTransactions.size());
        
        List<ReconciliationResult> results = matcher.reconcile(internalTransactions, statementLines);
        logger.info("Reconciliation completed with {} results", results.size());
        
        ReconciliationReport report = new ReconciliationReport(results);
        try {
            reportRepository.save(report, signature);
        } catch (Exception e) {
            throw new ReportStorageException("Failed to save reconciliation report", e);
        }
        logger.info("Reconciliation report saved");

        return report;
    }

    private String generateSignature(List<BankStatementLine> lines) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String content = lines.stream()
                    .sorted(Comparator.comparing(BankStatementLine::getReferenceId, Comparator.nullsLast(String::compareTo))
                            .thenComparing(BankStatementLine::getDate, Comparator.nullsLast(LocalDateTime::compareTo))
                            .thenComparing(BankStatementLine::getAmount, Comparator.nullsLast(BigDecimal::compareTo)))
                    .map(line -> String.valueOf(line.getReferenceId()) + "|" +
                                 String.valueOf(line.getAmount()) + "|" +
                                 String.valueOf(line.getDate()))
                    .collect(Collectors.joining("||"));
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}
