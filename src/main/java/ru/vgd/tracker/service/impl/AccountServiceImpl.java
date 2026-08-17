package ru.vgd.tracker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.dto.account.AccountCreateRequest;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.util.mapper.AccountMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final TransactionServiceImpl transactionService;

    /**
     * Получить все счета пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getUserAccounts(UUID userId) {
        List<Account> accounts = accountRepository.findAllByOwnersId(userId);
        return accountMapper.toDto(accounts);
    }

    /**
     * Получить счёт по ID с проверкой владения
     */
    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(UUID accountId, UUID userId) {
        RuntimeException e = new ItemNotFoundException("Счёт не найден");
        if (accountRepository.isAccountOwnedBy(userId, accountId)) {
            return accountRepository.findById(accountId).orElseThrow(() -> e);
        } else {
            throw e;
        }
    }

    /**
     * Создать новый счёт
     */
    @Override
    @Transactional
    public void createAccount(AccountCreateRequest request, User owner) {
        log.debug("Запрос на создание нового счёта. ownerId: {}, request: {}", owner.getId(), request);

        if (accountRepository.existsByOwnersIdAndName(owner.getId(), request.getName())) {
            throw new IllegalArgumentException(
                    "Счёт с названием «" + request.getName() + "» уже существует");
        }

        Account account = switch (request.getAccountType()) {
            case CASH -> accountMapper.toCashAccount(request, Set.of(owner));
            case BANK -> accountMapper.toBankAccount(request, Set.of(owner));
            case DEBIT_CARD ->  accountMapper.toDebitCardAccount(request, Set.of(owner));
            case CREDIT_CARD ->  accountMapper.toCreditCardAccount(request, Set.of(owner));
            case LOAN ->  accountMapper.toLoanAccount(request, Set.of(owner));
        };

        accountRepository.save(account);
        log.info("Создан новый счёт. id: {}, type: {}, ownerId: {}",
                account.getId(), account.getAccountType(), owner.getId());

        // Создание транзакции для начальной суммы на счёте
        if (!Objects.equals(request.getBalance(), BigDecimal.ZERO)) {
            transactionService.createAccountInitialTransaction(account);
        }
    }

    /**
     * Удаление счёта.
     * Счёт удаляется вместе со всеми транзакциями. Для связанных транзакций устанавливается значение related_transaction_id = NULL
     * Если пользователь единственный владелец, то счёт удаляется. Иначе пользователь удаляется из списка владельцев счёта.
     */
    @Override
    @Transactional
    public void deleteAccount(UUID accountId, User user) {
        log.debug("Запрос на удаление счёта. accountId: {}, userId: {}", accountId, user.getId());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ItemNotFoundException("Счёт не найден"));

        Set<User> owners = account.getOwners();
        if (!owners.contains(user)) {
            throw new AccessDeniedException("Нет прав для доступа к этому счёту");
        }

        if (owners.size() == 1) {
            accountRepository.delete(account);
            log.info("Счёт удалён. id: {}", accountId);
        } else {
            owners.remove(user);
            accountRepository.save(account);
            log.info("Для счёта accountId: {} удален владелец userId: {}", accountId, user.getId());
        }
    }
}
