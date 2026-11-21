package ru.vgd.tracker.util.mapper;

import org.mapstruct.*;
import ru.vgd.tracker.dal.account.entity.*;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.account.*;

import java.util.Collection;
import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION
)
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BankAccount toBankAccount(AccountCreateRequest request, User owner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CashAccount toCashAccount(AccountCreateRequest request, User owner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CreditCardAccount toCreditCardAccount(AccountCreateRequest request, User owner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DebitCardAccount toDebitCardAccount(AccountCreateRequest request, User owner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LoanAccount toLoanAccount(AccountCreateRequest request, User owner);

    @SubclassMapping(source = BankAccount.class, target = BankAccountDto.class)
    @SubclassMapping(source = CashAccount.class, target = CashAccountDto.class)
    @SubclassMapping(source = CreditCardAccount.class, target = CreditCardAccountDto.class)
    @SubclassMapping(source = DebitCardAccount.class, target = DebitCardAccountDto.class)
    @SubclassMapping(source = LoanAccount.class, target = LoanAccountDto.class)
    AccountDto toDto(Account entity);

    List<AccountDto> toDto(Collection<Account> entities);
}
