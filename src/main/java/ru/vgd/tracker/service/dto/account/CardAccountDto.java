package ru.vgd.tracker.service.dto.account;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import ru.vgd.tracker.dal.account.entity.CardType;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardAccountDto extends AccountDto {
    String bankName;

    String cardNumber;

    CardType cardType;

    Double creditLimit;
}
