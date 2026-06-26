package ru.vgd.tracker.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO для формы создания нового счёта
 */
@Getter
@Setter
public class CreateAccountRequest {

    @NotBlank(message = "Название счёта обязательно")
    private String name;

    @NotNull(message = "Тип счёта обязателен")
    private String accountType; // BANK, CARD, CASH

    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    private BigDecimal balance;

    private String bankName;

    private String accountNumber;

    private String cardNumber;

    private String cardType; // DEBIT, CREDIT

    private BigDecimal creditLimit;
}
