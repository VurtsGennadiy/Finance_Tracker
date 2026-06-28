package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.transaction.Transaction;
import ru.vgd.tracker.dal.transaction.TransactionRepository;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.exception.AccessDeniedException;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.service.dto.TransactionIncomeCreateRequest;
import ru.vgd.tracker.util.mapper.TransactionMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public void createIncomeTransaction(TransactionIncomeCreateRequest request, User user) {
        log.debug("Запрос на сохранение транзакции пополнения счёта. request: {}", request);

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ItemNotFoundException("Счёт не найден"));

        Set<User> owners = account.getOwners();
        if (!owners.contains(user)) {
            throw new AccessDeniedException(user.getUsername() + " не является владельцем счёта");
        }
        
        Transaction transaction = transactionMapper.fromIncomeCreateRequest(request, account);
        account.setBalance(account.getBalance().add(transaction.getAmount()));
        transactionRepository.save(transaction);
        accountRepository.save(account);

        log.info("Транзакция пополнения счёта сохранена. accountId: {}, transactionId: {}",
                account.getId(), transaction.getId());
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAccountTransactions(UUID accountId) {
        log.debug("Запрос на получение списка транзакций по счёту. accountId: {}", accountId);
        return transactionRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
