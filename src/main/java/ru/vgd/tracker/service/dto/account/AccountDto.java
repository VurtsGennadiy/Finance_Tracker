package ru.vgd.tracker.service.dto.account;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import ru.vgd.tracker.dal.account.entity.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AccountDto {

    @EqualsAndHashCode.Include
    UUID id;

    String name;

    BigDecimal balance;

    AccountType accountType;

    public AccountDto() {
    }

    public AccountDto(BigDecimal balance) {
        this.balance = balance;
    }
}
