package dev.cluuny.frc.infrastructure.adapter.out.persistence.entity;

import dev.cluuny.frc.domain.model.ReconciliationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_results")
@Data
@NoArgsConstructor
public class ReconciliationResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private ReconciliationReportEntity report;

    private String transactionReferenceId;
    private String bankReferenceId;
    
    @Enumerated(EnumType.STRING)
    private ReconciliationStatus status;

    // Snapshot of data at time of reconciliation
    private BigDecimal transactionAmount;
    private LocalDateTime transactionDate;
    private BigDecimal bankAmount;
    private LocalDateTime bankDate;
}
