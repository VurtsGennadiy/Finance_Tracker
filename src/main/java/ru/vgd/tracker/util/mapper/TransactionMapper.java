package ru.vgd.tracker.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.transaction.Transaction;
import ru.vgd.tracker.service.dto.TransactionCreateRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "INCOME")
    @Mapping(target = "createdAt", ignore = true)
    Transaction fromIncomeCreateRequest(TransactionCreateRequest request, Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "EXPENSE")
    @Mapping(target = "createdAt", ignore = true)
    Transaction fromExpenseCreateRequest(TransactionCreateRequest request, Account account);
}
