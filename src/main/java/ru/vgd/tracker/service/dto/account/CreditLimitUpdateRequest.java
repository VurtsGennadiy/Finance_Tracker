package ru.vgd.tracker.service.dto.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO для формы изменения кредитного лимита счёта кредитной карты.
 *
 * @param creditLimit новый кредитный лимит, не может быть отрицательным
 */
public record CreditLimitUpdateRequest(

        @NotNull(message = "Кредитный лимит обязателен")
        @DecimalMin(value = "0.00", message = "Кредитный лимит не может быть отрицательным")
        @Digits(integer = 12, fraction = 2, message = "Кредитный лимит должен содержать не более 2 знаков после запятой")
        BigDecimal creditLimit) {
}
