package ru.vgd.tracker.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.transaction.Transaction;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.TransactionCreateRequest;
import ru.vgd.tracker.service.dto.TransactionDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "INCOME")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "relatedTransaction", ignore = true)
    Transaction fromIncomeCreateRequest(TransactionCreateRequest request, Account account, User createdBy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "EXPENSE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "relatedTransaction", ignore = true)
    Transaction fromExpenseCreateRequest(TransactionCreateRequest request, Account account, User createdBy);

    @Mapping(target = "accountName", source = "transaction.account.name")
    TransactionDto toDto(Transaction transaction);

    List<TransactionDto> toDto(List<Transaction> transactions);
}
