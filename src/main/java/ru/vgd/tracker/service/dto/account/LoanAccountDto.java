package ru.vgd.tracker.service.dto.account;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.vgd.tracker.dal.account.entity.AccountType;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoanAccountDto extends AccountDto {

    public LoanAccountDto() {
        super();
        setAccountType(AccountType.LOAN);
    }

    public LoanAccountDto(BigDecimal balance) {
        super(balance);
        setAccountType(AccountType.LOAN);
    }
}
