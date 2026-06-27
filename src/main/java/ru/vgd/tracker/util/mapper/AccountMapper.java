package ru.vgd.tracker.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.vgd.tracker.dal.account.entity.BankAccount;
import ru.vgd.tracker.dal.account.entity.CardAccount;
import ru.vgd.tracker.dal.account.entity.CashAccount;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.CreateAccountRequest;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    BankAccount toBankAccount(CreateAccountRequest request, Set<User> owners);

    @Mapping(target = "id", ignore = true)
    CardAccount toCardAccount(CreateAccountRequest request, Set<User> owners);

    @Mapping(target = "id", ignore = true)
    CashAccount toCashAccount(CreateAccountRequest request, Set<User> owners);
}
