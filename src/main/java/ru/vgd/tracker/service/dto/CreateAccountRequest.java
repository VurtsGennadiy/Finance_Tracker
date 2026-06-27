package ru.vgd.tracker.service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.vgd.tracker.dal.account.entity.AccountType;
import ru.vgd.tracker.dal.account.entity.CardType;

import java.math.BigDecimal;

/**
 * DTO для формы создания нового счёта
 */
@Data
public class CreateAccountRequest {

    @NotBlank(message = "Название счёта обязательно")
    private String name;

    @NotNull(message = "Тип счёта обязателен")
    private AccountType accountType = AccountType.CASH;

    @NotNull
    private BigDecimal balance = BigDecimal.ZERO;

    private String bankName;

    private String accountNumber;

    private String cardNumber;

    private CardType cardType = CardType.DEBIT;

    @DecimalMin(value = "0.00", message = "Кредитный лимит не может быть отрицательным")
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @AssertTrue(message = "Название банка должно быть не пустым")
    public boolean isBankNameValid() {
        if (accountType != AccountType.CASH) {
            if (bankName == null) return false;
            else return !bankName.isBlank();
        }
        return true;
    }
}
