package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.transaction.Category;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.exception.AccountDeleteException;
import ru.vgd.tracker.service.dto.CreateAccountRequest;
import ru.vgd.tracker.service.dto.TransactionCreateRequest;
import ru.vgd.tracker.util.mapper.AccountMapper;
import ru.vgd.tracker.exception.AccessDeniedException;
import ru.vgd.tracker.exception.ItemNotFoundException;

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
    public List<Account> getUserAccounts(UUID userId) {
        return accountRepository.findAllByOwnersId(userId);
    }

    /**
     * Получить счёт по ID с проверкой владения
     */
    @Transactional(readOnly = true)
    public Account getAccountById(UUID accountId, UUID userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ItemNotFoundException("Счёт не найден"));
        
        if (account.getOwners().stream().noneMatch(user -> user.getId().equals(userId))) {
            throw new AccessDeniedException("Нет прав для доступа к этому счёту");
        }
        
        return account;
    }

    /**
     * Создать новый счёт
     */
    @Transactional
    public Account createAccount(CreateAccountRequest request, User owner) {
        log.debug("Запрос на создание нового счёта. ownerId: {}, request: {}", owner.getId(), request);

        if (accountRepository.existsByOwnersIdAndName(owner.getId(), request.getName())) {
            throw new IllegalArgumentException(
                    "Счёт с названием «" + request.getName() + "» уже существует");
        }

        Account account = switch (request.getAccountType()) {
            case BANK -> accountMapper.toBankAccount(request, Set.of(owner));
            case CARD -> accountMapper.toCardAccount(request, Set.of(owner));
            case CASH -> accountMapper.toCashAccount(request, Set.of(owner));
        };

        accountRepository.save(account);
        log.info("Создан новый счёт. id: {}, type: {}, ownerId: {}",
                account.getId(), account.getAccountType(), owner.getId());

        // Создание транзакции для начальной суммы на счёте
        if (!Objects.equals(request.getBalance(), BigDecimal.ZERO)) {
            TransactionCreateRequest transaction = new TransactionCreateRequest();
            transaction.setAccountId(account.getId());
            transaction.setDescription("Начальный баланс");
            transaction.setAmount(request.getBalance().abs());

            transaction.setCategory(ru.vgd.tracker.dal.transaction.Category.INCOME_OTHER);
            if (request.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                transaction.setCategory(Category.INCOME_OTHER);
                transactionService.createIncomeTransaction(transaction, owner);
            } else {
                transaction.setCategory(Category.EXPENSE_OTHER);
                transactionService.createExpenseTransaction(transaction, owner);
            }
        }
        return account;
    }

    /**
     * Удаление счёта
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

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountDeleteException("Удаление счёта с ненулевым балансом запрещено");
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
