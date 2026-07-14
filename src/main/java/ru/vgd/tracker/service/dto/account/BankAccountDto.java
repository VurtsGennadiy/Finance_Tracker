package ru.vgd.tracker.service.dto.account;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BankAccountDto extends AccountDto {
    String bankName;
    String accountNumber;
}
