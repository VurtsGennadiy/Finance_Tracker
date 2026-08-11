package ru.vgd.tracker.service.dto.account;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import ru.vgd.tracker.dal.account.entity.AccountType;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditCardAccountDto extends AccountDto {

    String bankName;
    String cardNumber;
    BigDecimal creditLimit;

    public CreditCardAccountDto() {
        super();
        setAccountType(AccountType.CREDIT_CARD);
    }

    public CreditCardAccountDto(BigDecimal balance, BigDecimal creditLimit) {
        super(balance);
        setAccountType(AccountType.CREDIT_CARD);
        this.creditLimit = creditLimit;
    }
}
