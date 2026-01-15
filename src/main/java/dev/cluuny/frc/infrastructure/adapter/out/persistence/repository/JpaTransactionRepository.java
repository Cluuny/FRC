package dev.cluuny.frc.infrastructure.adapter.out.persistence.repository;

import dev.cluuny.frc.infrastructure.adapter.out.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, String> {
}
