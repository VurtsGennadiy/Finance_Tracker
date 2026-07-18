package ru.vgd.tracker.dal.transaction;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.dal.user.UserRepository;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.transaction.TransactionFilter;

import java.util.UUID;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TransactionRepositoryTest {
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

/*    @Test
    public void test() {
        TransactionFilter filter = new TransactionFilter();
        filter.setAccounts(List.of(UUID.fromString("bb6e714a-d671-4454-a772-a9d82c9b47c5")));

        User user = userRepository.findById(UUID.fromString("a7cd6ec5-f4b0-4773-a815-ab9555676417")).get();


*//*        var spec = TransactionSpecifications
                .accountOwnerId(user.getId())
                .and(TransactionSpecifications.filter(filter));*//*

        var spec = TransactionSpecifications.withFilter(filter);

        List<Transaction> transactions = transactionRepository.findAll(spec, PageRequest.of(0, 20)).getContent();

        assertEquals(2, transactions.size());
    }*/
}
