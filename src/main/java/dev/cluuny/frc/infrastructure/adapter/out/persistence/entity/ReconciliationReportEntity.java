package dev.cluuny.frc.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_reports")
@Data
@NoArgsConstructor
public class ReconciliationReportEntity {
    @Id
    private String id;
    private LocalDateTime processedAt;
    private String processedBy; // For audit

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "report")
    private List<ReconciliationResultEntity> results;

    public ReconciliationReportEntity(String processedBy) {
        this.id = UUID.randomUUID().toString();
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
    }
}
