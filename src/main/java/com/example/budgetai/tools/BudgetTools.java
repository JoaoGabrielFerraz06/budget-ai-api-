package com.example.budgetai.tools;

import com.example.budgetai.domain.Transaction;
import com.example.budgetai.domain.TransactionType;
import com.example.budgetai.service.BudgetService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Funções reais da aplicação expostas ao LLM (Tool Calling).
 * O modelo decide quando chamá-las a partir do comando do usuário.
 */
@Component
public class BudgetTools {

    private final BudgetService budgetService;

    public BudgetTools(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Tool(description = "Registra uma nova transação financeira (receita ou despesa) no orçamento.")
    public String registerTransaction(
            @ToolParam(description = "Descrição curta, ex: 'mercado', 'salário'") String description,
            @ToolParam(description = "Valor positivo em reais, ex: 49.90") BigDecimal amount,
            @ToolParam(description = "INCOME para receita ou EXPENSE para despesa") TransactionType type,
            @ToolParam(description = "Categoria, ex: alimentação, transporte, lazer", required = false) String category,
            @ToolParam(description = "Data no formato yyyy-MM-dd. Se omitida, usa hoje", required = false) LocalDate date) {
        Transaction t = budgetService.register(description, amount, type, category, date, "ASSISTANT");
        return "Transação #" + t.getId() + " registrada: " + t.getType() + " de R$ " + t.getAmount()
                + " (" + t.getDescription() + ") em " + t.getDate();
    }

    @Tool(description = "Retorna o saldo atual (receitas menos despesas).")
    public String getBalance() {
        return "Saldo atual: R$ " + budgetService.balance();
    }

    @Tool(description = "Retorna o total de receitas ou de despesas já registradas.")
    public String getTotalByType(
            @ToolParam(description = "INCOME ou EXPENSE") TransactionType type) {
        return "Total de " + type + ": R$ " + budgetService.total(type);
    }

    @Tool(description = "Lista as transações em um período.")
    public List<Transaction> listTransactions(
            @ToolParam(description = "Data inicial yyyy-MM-dd") LocalDate from,
            @ToolParam(description = "Data final yyyy-MM-dd") LocalDate to) {
        return budgetService.list(from, to);
    }

    @Tool(description = "Retorna a data de hoje, útil para interpretar 'hoje', 'ontem', 'este mês'.")
    public String today() {
        return LocalDate.now().toString();
    }
}
