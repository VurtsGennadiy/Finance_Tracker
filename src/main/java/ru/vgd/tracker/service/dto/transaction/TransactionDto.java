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

    private UUID id;

    private String accountName;

    private BigDecimal amount;

    private TransactionType type;

    private Category category;

    private String description;

    private LocalDate transactionDate;
}
