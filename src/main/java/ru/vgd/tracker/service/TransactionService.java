package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.transaction.Transaction;
import ru.vgd.tracker.dal.transaction.TransactionRepository;
import ru.vgd.tracker.dal.transaction.TransactionType;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.service.dto.TransactionCreateRequest;
import ru.vgd.tracker.service.dto.TransferCreateRequest;
import ru.vgd.tracker.util.mapper.TransactionMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public void createIncomeTransaction(TransactionCreateRequest request, User user) {
        log.debug("Запрос на сохранение транзакции пополнения счёта. request: {}", request);

        Account account = getAccountAndCheckOwner(request.getAccountId(), user);
        Transaction transaction = transactionMapper.fromIncomeCreateRequest(request, account);
        account.setBalance(account.getBalance().add(transaction.getAmount()));
        transactionRepository.save(transaction);
        accountRepository.save(account);

        log.info("Транзакция пополнения счёта сохранена. accountId: {}, transactionId: {}",
                account.getId(), transaction.getId());
    }

    @Transactional
    public void createExpenseTransaction(TransactionCreateRequest request, User user) {
        log.debug("Запрос на сохранение транзакции расхода. request: {}", request);

        Account account = getAccountAndCheckOwner(request.getAccountId(), user);
        Transaction transaction = transactionMapper.fromExpenseCreateRequest(request, account);
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        transactionRepository.save(transaction);
        accountRepository.save(account);

        log.info("Транзакция расхода сохранена. accountId: {}, transactionId: {}",
                account.getId(), transaction.getId());
    }

    @Transactional
    public void createTransferTransactions(TransferCreateRequest request, User user) {
        log.debug("Запрос на создание денежного перевода между счетами. userId: {}, request: {}",
                user.getId(), request);

        Account fromAccount = getAccountAndCheckOwner(request.getFromAccountId(), user);
        Account toAccount = getAccountAndCheckOwner(request.getToAccountId(), user);

        String fromTransactionComment = String.format("Перевод на счёт «%s»", toAccount.getName());
        String toTransactionComment = String.format("Перевод со счёта «%s»", fromAccount.getName());
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            fromTransactionComment = fromTransactionComment + ". " + request.getDescription();
            toTransactionComment = toTransactionComment + ". " + request.getDescription();
        }

        Transaction fromTransaction = new Transaction();
        fromTransaction.setAccount(fromAccount);
        fromTransaction.setAmount(request.getAmount());
        fromTransaction.setType(TransactionType.TRANSFER_OUT);
        fromTransaction.setDescription(fromTransactionComment);

        Transaction toTransaction = new Transaction();
        toTransaction.setAccount(toAccount);
        toTransaction.setAmount(request.getAmount());
        toTransaction.setType(TransactionType.TRANSFER_IN);
        toTransaction.setDescription(toTransactionComment);

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        transactionRepository.saveAll(List.of(fromTransaction, toTransaction));

        log.info("Сохранены транзакции перевода между счетами." +
                        " from accountId: {} transactionId: {}; to accountId: {}, transactionId: {}",
                fromAccount.getId(), fromTransaction.getId(), toAccount.getId(), toTransaction.getId());
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAccountTransactions(UUID accountId) {
        log.debug("Запрос на получение списка транзакций по счёту. accountId: {}", accountId);
        return transactionRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId);
    }

    private Account getAccountAndCheckOwner(UUID accountId, User user) {
        RuntimeException e = new ItemNotFoundException("Счёт не найден");
        if (accountRepository.isAccountOwnedBy(user.getId(), accountId)) {
            return accountRepository.findById(accountId).orElseThrow(() -> e);
        } else {
            throw e;
        }
    }
}
