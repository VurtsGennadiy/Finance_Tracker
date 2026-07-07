package ru.vgd.tracker.service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO для формы создания денежного перевода между счетами
 */
@Data
public class TransferCreateRequest {

    @NotNull
    private UUID fromAccountId;

    @NotNull
    private UUID toAccountId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть не менее 0.01")
    private BigDecimal amount;

    private String description;

    private LocalDate transactionDate;

    @AssertTrue(message = "Исходный и целевой счета должны различаться")
    public boolean isAccountsDifferent() {
        return !fromAccountId.equals(toAccountId);
    }

}
