package ru.vgd.tracker.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.vgd.tracker.dal.account.entity.BankAccount;
import ru.vgd.tracker.dal.account.entity.CardAccount;
import ru.vgd.tracker.dal.account.entity.CashAccount;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.AccountCreateRequest;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    BankAccount toBankAccount(AccountCreateRequest request, Set<User> owners);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    CardAccount toCardAccount(AccountCreateRequest request, Set<User> owners);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    CashAccount toCashAccount(AccountCreateRequest request, Set<User> owners);
}
