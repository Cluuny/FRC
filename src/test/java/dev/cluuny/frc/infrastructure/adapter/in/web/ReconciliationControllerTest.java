package dev.cluuny.frc.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cluuny.frc.application.port.in.ReconcileStatementUseCase;
import dev.cluuny.frc.domain.model.ReconciliationReport;
import dev.cluuny.frc.domain.model.ReconciliationResult;
import dev.cluuny.frc.domain.model.ReconciliationStatus;
import dev.cluuny.frc.infrastructure.adapter.in.web.dto.BankStatementDto;
import dev.cluuny.frc.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReconciliationController.class)
@Import(SecurityConfig.class)
class ReconciliationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReconcileStatementUseCase reconcileStatementUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldProcessStatement() throws Exception {
        BankStatementDto dto = new BankStatementDto();
        dto.setReferenceId("REF1");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDate(LocalDateTime.now());

        ReconciliationReport report = new ReconciliationReport(List.of(
                new ReconciliationResult(null, null, ReconciliationStatus.MATCHED)
        ));

        when(reconcileStatementUseCase.reconcile(anyList())).thenReturn(report);

        mockMvc.perform(post("/api/v1/reconciliation/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(dto)))
                        .header("Idempotency-Key", "key-123"))
                .andExpect(status().isOk());
    }
}
