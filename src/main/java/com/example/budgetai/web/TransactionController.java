package com.example.budgetai.web;

import com.example.budgetai.domain.Transaction;
import com.example.budgetai.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final BudgetService service;

    public TransactionController(BudgetService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody TransactionRequest req) {
        return service.register(req.description(), req.amount(), req.type(),
                req.category(), req.date(), "API");
    }

    @GetMapping
    public List<Transaction> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return service.list(from, to);
        }
        return service.listAll();
    }

    @GetMapping("/balance")
    public Map<String, BigDecimal> balance() {
        return Map.of("balance", service.balance());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
