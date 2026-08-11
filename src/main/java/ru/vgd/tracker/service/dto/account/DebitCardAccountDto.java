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
public class DebitCardAccountDto extends AccountDto {
    String bankName;

    String cardNumber;

    public DebitCardAccountDto() {
        super();
        setAccountType(AccountType.DEBIT_CARD);
    }

    public DebitCardAccountDto(BigDecimal balance) {
        super(balance);
        setAccountType(AccountType.DEBIT_CARD);
    }
}
