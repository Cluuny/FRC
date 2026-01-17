package dev.cluuny.frc.infrastructure.config;

import dev.cluuny.frc.domain.model.ReconciliationPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReconciliationPolicyProperties.class)
public class ReconciliationPolicyConfig {
    @Bean
    public ReconciliationPolicy reconciliationPolicy(
            ReconciliationPolicyProperties props
    ) {
        return new ReconciliationPolicy(
                props.getExactToleranceSeconds(),
                props.getMaxDayTolerance(),
                props.isAllowAmountFallback(),
                props.isAllowPotentialMatch()
        );
    }
}
