package ru.vgd.tracker.facade.transaction;

import org.springframework.data.domain.Pageable;
import ru.vgd.tracker.service.dto.transaction.TransactionFilter;

import java.util.UUID;

public interface TransactionsFacade {
    TransactionPageResponse getTransactionPageData(UUID userId, TransactionFilter filter, Pageable pageable);
}
