package ru.vgd.tracker.util.mapper;

import org.mapstruct.*;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.entity.BankAccount;
import ru.vgd.tracker.dal.account.entity.CardAccount;
import ru.vgd.tracker.dal.account.entity.CashAccount;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.account.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION
)
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    BankAccount toBankAccount(AccountCreateRequest request, Set<User> owners);

    @Mapping(target = "id", ignore = true)
    CardAccount toCardAccount(AccountCreateRequest request, Set<User> owners);

    @Mapping(target = "id", ignore = true)
    CashAccount toCashAccount(AccountCreateRequest request, Set<User> owners);

    @SubclassMapping(source = BankAccount.class, target = BankAccountDto.class)
    @SubclassMapping(source = CardAccount.class, target = CardAccountDto.class)
    @SubclassMapping(source = CashAccount.class, target = CashAccountDto.class)
    AccountDto toDto(Account entity);

    List<AccountDto> toDto(Collection<Account> entities);
}
