package com.example.budgetai.service;

import com.example.budgetai.domain.Transaction;
import com.example.budgetai.domain.TransactionType;
import com.example.budgetai.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Regras de negócio do orçamento. Usado tanto pelo REST quanto pelas Tools da IA. */
@Service
public class BudgetService {

    private final TransactionRepository repository;

    public BudgetService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Transaction register(String description, BigDecimal amount, TransactionType type,
                                String category, LocalDate date, String source) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        return repository.save(new Transaction(description, amount, type, category, effectiveDate, source));
    }

    @Transactional(readOnly = true)
    public List<Transaction> list(LocalDate from, LocalDate to) {
        return repository.findByDateBetweenOrderByDateDesc(from, to);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public BigDecimal balance() {
        return repository.sumByType(TransactionType.INCOME)
                .subtract(repository.sumByType(TransactionType.EXPENSE));
    }

    @Transactional(readOnly = true)
    public BigDecimal total(TransactionType type) {
        return repository.sumByType(type);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
