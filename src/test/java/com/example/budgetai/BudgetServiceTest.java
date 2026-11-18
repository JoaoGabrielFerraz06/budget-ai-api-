package com.example.budgetai;

import com.example.budgetai.domain.TransactionType;
import com.example.budgetai.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(BudgetService.class)
class BudgetServiceTest {

    @Autowired BudgetService service;

    @Test
    void balanceIsIncomeMinusExpense() {
        service.register("salário", new BigDecimal("3000.00"), TransactionType.INCOME, "renda", null, "API");
        service.register("mercado", new BigDecimal("450.50"), TransactionType.EXPENSE, "alimentação", null, "API");
        assertEquals(0, new BigDecimal("2549.50").compareTo(service.balance()));
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                service.register("x", BigDecimal.ZERO, TransactionType.EXPENSE, null, null, "API"));
    }
}
