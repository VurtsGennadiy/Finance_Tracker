package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.transaction.*;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.service.dto.transaction.TransactionCreateRequest;
import ru.vgd.tracker.service.dto.transaction.TransactionDto;
import ru.vgd.tracker.service.dto.transaction.TransactionFilter;
import ru.vgd.tracker.service.dto.transaction.TransferCreateRequest;
import ru.vgd.tracker.util.mapper.TransactionMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Добавление доходной транзакции
     */
    @Transactional
    public void createIncomeTransaction(TransactionCreateRequest request, User user) {
        log.debug("Запрос на сохранение транзакции пополнения счёта. request: {}", request);

        Account account = getAccountAndCheckOwner(request.getAccountId(), user);
        Transaction transaction = transactionMapper.fromIncomeCreateRequest(request, account, user);
        account.setBalance(account.getBalance().add(transaction.getAmount()));
        transactionRepository.save(transaction);
        accountRepository.save(account);

        log.info("Транзакция пополнения счёта сохранена. accountId: {}, transactionId: {}",
                account.getId(), transaction.getId());
    }

    /**
     * Добавление расходной транзакции
     */
    @Transactional
    public void createExpenseTransaction(TransactionCreateRequest request, User user) {
        log.debug("Запрос на сохранение транзакции расхода. request: {}", request);

        Account account = getAccountAndCheckOwner(request.getAccountId(), user);
        Transaction transaction = transactionMapper.fromExpenseCreateRequest(request, account, user);
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        transactionRepository.save(transaction);
        accountRepository.save(account);

        log.info("Транзакция расхода сохранена. accountId: {}, transactionId: {}",
                account.getId(), transaction.getId());
    }

    /**
     * Добавление перевода средств между счетами
     */
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
        fromTransaction.setTransactionDate(request.getTransactionDate());
        fromTransaction.setCreatedBy(user);

        Transaction toTransaction = new Transaction();
        toTransaction.setAccount(toAccount);
        toTransaction.setAmount(request.getAmount());
        toTransaction.setType(TransactionType.TRANSFER_IN);
        toTransaction.setDescription(toTransactionComment);
        toTransaction.setTransactionDate(request.getTransactionDate());
        toTransaction.setCreatedBy(user);

        // Связываем транзакции перевода
        fromTransaction.setRelatedTransaction(toTransaction);
        toTransaction.setRelatedTransaction(fromTransaction);

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        transactionRepository.saveAll(List.of(fromTransaction, toTransaction));

        log.info("Сохранены транзакции перевода между счетами." +
                        " from accountId: {} transactionId: {}; to accountId: {}, transactionId: {}",
                fromAccount.getId(), fromTransaction.getId(), toAccount.getId(), toTransaction.getId());
    }

    /**
     * Получение последних транзакций по счёту.
     * @param accountId идентификатор счёта.
     * @return List<TransactionDto> из 5 последних транзакций отсортированных по дате совершения транзакции.
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getAccountLastTransactions(UUID accountId) {
        final int limit = 5;
        log.debug("Запрос на получение {} последних транзакций по счёту. accountId: {}", limit, accountId);
        List<Transaction> transactions = transactionRepository.findByAccountId(
                accountId, Limit.of(limit), TransactionSort.DEFAULT.getSortValue());
        return transactionMapper.toDto(transactions);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getUserLastTransactions(UUID userId) {
        final int limit = 5;
        log.debug("Запрос на получение {} последних транзакций для пользователя. userId: {}", limit, userId);
        List<Transaction> transactions = transactionRepository.findByAccountOwnersId(
                userId, Limit.of(limit), TransactionSort.DEFAULT.getSortValue());
        return transactionMapper.toDto(transactions);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactions(TransactionFilter filter, Pageable pageable) {
        log.debug("Поиск транзакций {}, {} ", filter, pageable);
        var spec = TransactionSpecifications.withFilter(filter);
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);
        return transactionPage.map(transactionMapper::toDto);
    }

    @Transactional
    public void cancelTransaction(UUID transactionId) {
        log.debug("Запрос отмены транзакции. transactionId: {}", transactionId);

        Transaction lastTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ItemNotFoundException("Транзакция не найдена"));

        BigDecimal amount = lastTransaction.getAmount();
        Account account = lastTransaction.getAccount();

        switch (lastTransaction.getType()) {
            case INCOME -> account.setBalance(account.getBalance().subtract(amount));
            case EXPENSE -> account.setBalance(account.getBalance().add(amount));
            case TRANSFER_IN, TRANSFER_OUT -> {
                if (TransactionType.TRANSFER_IN == lastTransaction.getType()) {
                    account.setBalance(account.getBalance().subtract(amount));
                } else {
                    account.setBalance(account.getBalance().add(amount));
                }

                Transaction relatedTransaction = lastTransaction.getRelatedTransaction();
                if (relatedTransaction != null) {
                    Account relatedAccount = relatedTransaction.getAccount();
                    if (TransactionType.TRANSFER_IN == relatedTransaction.getType()) {
                        relatedAccount.setBalance(account.getBalance().subtract(amount));
                    } else {
                        relatedAccount.setBalance(account.getBalance().add(amount));
                    }
                    transactionRepository.delete(relatedTransaction);
                    log.info("Отменена связанная транзакция. transactionId: {}", relatedTransaction.getId());
                }
            }
        }

        transactionRepository.delete(lastTransaction);
        log.info("Транзакция отменена. transactionId: {}", transactionId);
    }

    /**
     * Отмена последней транзакции пользователя.
     * @param userId идентификатор пользователя, совершившего транзакцию.
     */
    @Transactional
    public void cancelLastTransaction(UUID userId) {
        log.debug("Запрос на отмену последней транзакции пользователя. userId: {}", userId);
        Pageable pageable = PageRequest.of(0, 1, TransactionSort.CREATED_AT_DESC.getSortValue());
        Page<Transaction> transactionsPage = transactionRepository.findByCreatedById(userId, pageable);
        Transaction lastTransaction = transactionsPage.getContent().stream().findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Транзакция не найдена"));

        cancelTransaction(lastTransaction.getId());
    }

    /**
     * Сохранение начальной транзакции пополнения счёта при создании счёта.
     * @param account только созданный счёт. У счёта должен быть только один владелец и не должно существовать транзакций по счёту.
     * @throws IllegalStateException в случае, если у счёта не один владелец или уже существуют транзакции по счёту.
     */
    @Transactional
    public void createAccountInitialTransaction(Account account) {
        log.debug("Создание начальной транзакции для счёта accountId: {}", account.getId());

        if (account.getOwners().size() != 1) {
            throw new IllegalStateException("Невозможно создать начальную транзакцию для счёта accountId: "
                    + account.getId() + " . Причина: у счёта должен быть единственный владелец.");
        }

        if (transactionRepository.countByAccountId(account.getId()) > 0) {
            throw new IllegalStateException("Невозможно создать начальную транзакцию для счёта accountId: "
                    + account.getId() + " . Причина: счёт уже имеет транзакции.");
        }

        User initiator = account.getOwners().stream().findFirst().orElseThrow();
        Transaction transaction = new Transaction();
        transaction.setCreatedBy(initiator);
        transaction.setAccount(account);
        transaction.setAmount(account.getBalance());
        transaction.setType(TransactionType.INITIAL);
        transaction.setDescription("Создание счёта");
        transactionRepository.save(transaction);

        log.info("Сохранена начальная транзакция account Id: {}, transactionId: {}", account.getId(), transaction.getId());
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
