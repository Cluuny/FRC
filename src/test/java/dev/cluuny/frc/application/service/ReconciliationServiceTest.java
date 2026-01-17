package dev.cluuny.frc.application.service;

import dev.cluuny.frc.application.port.out.ReconciliationReportRepositoryPort;
import dev.cluuny.frc.application.port.out.TransactionRepositoryPort;
import dev.cluuny.frc.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReconciliationServiceTest {

    private TransactionRepositoryPort transactionRepository;
    private ReconciliationReportRepositoryPort reportRepository;
    private ReconciliationService service;

    @BeforeEach
    void setUp() {
        transactionRepository =
                Mockito.mock(TransactionRepositoryPort.class);
        reportRepository =
                Mockito.mock(ReconciliationReportRepositoryPort.class);
        ReconciliationPolicy reconciliationPolicy = Mockito.mock(ReconciliationPolicy.class);

        service = new ReconciliationService(
                reconciliationPolicy,
                transactionRepository,
                reportRepository
        );
    }

    @Test
    void shouldReconcileAndSaveReport() {
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 =
                new Transaction("REF1", new BigDecimal("100.00"), now);
        when(transactionRepository.findAll())
                .thenReturn(List.of(t1));

        BankStatementLine b1 =
                new BankStatementLine("REF1", new BigDecimal("100.00"), now);

        ReconciliationReport report =
                service.reconcile(List.of(b1));

        assertEquals(1, report.getResults().size());

        assertEquals(
                ReconciliationStatus.RECONCILED_EXACT,
                report.getResults().get(0).getStatus()
        );

        ArgumentCaptor<ReconciliationReport> captor =
                ArgumentCaptor.forClass(ReconciliationReport.class);

        verify(reportRepository).save(captor.capture());

        assertEquals(
                1,
                captor.getValue().getResults().size()
        );
    }
}
