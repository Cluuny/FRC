package dev.cluuny.frc.application.port.out;

import dev.cluuny.frc.domain.model.Transaction;

import java.util.List;

public interface TransactionRepositoryPort {
    List<Transaction> findAll();
    // In a real app, we would filter by date range or other criteria
}
