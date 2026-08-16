package ru.vgd.tracker.service.dto.transaction;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.vgd.tracker.dal.transaction.Category;
import ru.vgd.tracker.dal.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionDto {

    UUID id;

    String accountName;

    BigDecimal amount;

    TransactionType type;

    Category category;

    String description;

    LocalDate transactionDate;
}
