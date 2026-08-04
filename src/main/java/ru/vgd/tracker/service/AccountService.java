package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.exception.AccessDeniedException;
import ru.vgd.tracker.exception.ItemNotFoundException;
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
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final TransactionService transactionService;

    /**
     * Получить все счета пользователя
     */
    @Transactional(readOnly = true)
    public List<AccountDto> getUserAccounts(UUID userId) {
        List<Account> accounts = accountRepository.findAllByOwnersId(userId);
        return accountMapper.toDto(accounts);
    }

    /**
     * Получить счёт по ID с проверкой владения
     */
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
    @Transactional
    public void createAccount(AccountCreateRequest request, User owner) {
        log.debug("Запрос на создание нового счёта. ownerId: {}, request: {}", owner.getId(), request);

        if (accountRepository.existsByOwnersIdAndName(owner.getId(), request.getName())) {
            throw new IllegalArgumentException(
                    "Счёт с названием «" + request.getName() + "» уже существует");
        }

        Account account = switch (request.getAccountType()) {
            case BANK -> accountMapper.toBankAccount(request, Set.of(owner));
            case CARD -> accountMapper.toCardAccount(request, Set.of(owner));
            case CASH -> accountMapper.toCashAccount(request, Set.of(owner));
            // TODO
            default -> throw new IllegalStateException();
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
