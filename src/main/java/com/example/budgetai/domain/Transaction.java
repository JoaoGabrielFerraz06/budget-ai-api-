package com.example.budgetai.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String category;

    @Column(nullable = false)
    private LocalDate date;

    // Auditoria: quando e por qual canal o registro foi criado
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false, updatable = false)
    private String source = "API"; // API | ASSISTANT

    protected Transaction() {}

    public Transaction(String description, BigDecimal amount, TransactionType type,
                       String category, LocalDate date, String source) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.source = source;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public Instant getCreatedAt() { return createdAt; }
    public String getSource() { return source; }
}
