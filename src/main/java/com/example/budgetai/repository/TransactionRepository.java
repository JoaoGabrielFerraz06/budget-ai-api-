package com.example.budgetai.repository;

import com.example.budgetai.domain.Transaction;
import com.example.budgetai.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);
}
