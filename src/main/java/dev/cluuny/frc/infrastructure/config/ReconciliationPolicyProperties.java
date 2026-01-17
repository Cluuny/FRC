package dev.cluuny.frc.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Setter
@Getter
@ConfigurationProperties(prefix = "reconciliation.policy")
public class ReconciliationPolicyProperties {
    private Duration exactToleranceSeconds = Duration.ofSeconds(60);
    private int maxDayTolerance = 2;
    private boolean allowAmountFallback = true;
    private boolean allowPotentialMatch = true;

}
