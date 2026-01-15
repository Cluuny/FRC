package dev.cluuny.frc.infrastructure.adapter.out.persistence;

import dev.cluuny.frc.application.port.out.TransactionRepositoryPort;
import dev.cluuny.frc.domain.model.Transaction;
import dev.cluuny.frc.infrastructure.adapter.out.persistence.entity.TransactionEntity;
import dev.cluuny.frc.infrastructure.adapter.out.persistence.repository.JpaTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final JpaTransactionRepository jpaTransactionRepository;

    public TransactionPersistenceAdapter(JpaTransactionRepository jpaTransactionRepository) {
        this.jpaTransactionRepository = jpaTransactionRepository;
    }

    @Override
    public List<Transaction> findAll() {
        return jpaTransactionRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(entity.getReferenceId(), entity.getAmount(), entity.getDate());
    }
}
