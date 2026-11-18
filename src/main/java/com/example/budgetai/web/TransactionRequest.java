package com.example.budgetai.web;

import com.example.budgetai.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull TransactionType type,
        String category,
        LocalDate date) {}
