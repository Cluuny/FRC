package dev.cluuny.frc.domain.model;

import lombok.Getter;

import java.util.List;

@Getter
public class ReconciliationReport {
    private final List<ReconciliationResult> results;

    public ReconciliationReport(List<ReconciliationResult> results) {
        this.results = results;
    }
}
