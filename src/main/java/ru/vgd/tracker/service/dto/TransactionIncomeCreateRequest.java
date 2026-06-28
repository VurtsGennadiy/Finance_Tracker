package ru.vgd.tracker.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.vgd.tracker.dal.transaction.Category;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для формы создания входящей транзакции (пополнение счёта)
 */
@Data
public class TransactionIncomeCreateRequest {

    @NotNull
    private UUID accountId;

    @DecimalMin(value = "0.01", message = "Сумма пополнения должна быть не менее 0.01")
    private BigDecimal amount;

    @NotNull(message = "Категория не может быть пустой")
    private Category category;

    private String description;

    public TransactionIncomeCreateRequest() {
    }

    public TransactionIncomeCreateRequest(UUID accountId) {
        this.accountId = accountId;
    }
}
