package ru.vgd.tracker.service.dto.account;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.vgd.tracker.dal.account.entity.AccountType;

import java.math.BigDecimal;
import java.util.Set;

/**
 * DTO для формы создания нового счёта
 */
@Data
public class AccountCreateRequest {

    @NotBlank(message = "Название счёта обязательно")
    private String name;

    @NotNull(message = "Тип счёта обязателен")
    private AccountType accountType = AccountType.CASH;

    @NotNull
    private BigDecimal balance = BigDecimal.ZERO;

    private String bankName;

    private String accountNumber;

    private String cardNumber;

    @DecimalMin(value = "0.00", message = "Кредитный лимит не может быть отрицательным")
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @AssertTrue(message = "Название банка не должно быть пустым")
    public boolean isBankNameValid() {
        final Set<AccountType> checkedTypes = Set.of(
                AccountType.DEBIT_CARD,
                AccountType.CREDIT_CARD,
                AccountType.BANK
        );

        if (checkedTypes.contains(accountType)) {
            if (bankName == null) return false;
            else return !bankName.isBlank();
        }
        return true;
    }
}
