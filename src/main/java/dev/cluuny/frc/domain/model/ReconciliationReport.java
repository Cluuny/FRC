package dev.cluuny.rfc.domain.model;

import java.util.List;

public class ReconciliationReport {
    private List<ReconciliationResult> results;

    public ReconciliationReport(List<ReconciliationResult> results) {
        this.results = results;
    }

    public List<ReconciliationResult> getResults() {
        return results;
    }
}
