package dev.cluuny.frc.infrastructure.adapter.in.web.dto;

import dev.cluuny.frc.domain.model.ReconciliationReport;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ReconciliationReportDto {
    private List<ReconciliationResultDto> results;

    public static ReconciliationReportDto fromDomain(ReconciliationReport report) {
        ReconciliationReportDto dto = new ReconciliationReportDto();
        dto.setResults(report.getResults().stream()
                .map(ReconciliationResultDto::fromDomain)
                .collect(Collectors.toList()));
        return dto;
    }
}
