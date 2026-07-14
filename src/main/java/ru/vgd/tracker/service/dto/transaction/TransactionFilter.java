package ru.vgd.tracker.service.dto.transaction;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionFilter {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateFrom = LocalDate.now().minusDays(7);

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateTo = LocalDate.now();

    List<UUID> accounts = new ArrayList<>();

    boolean includeTransfers = true;

    String period = "7";
}
