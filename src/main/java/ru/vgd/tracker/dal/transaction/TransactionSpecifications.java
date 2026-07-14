package ru.vgd.tracker.dal.transaction;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.transaction.TransactionFilter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@UtilityClass
public final class TransactionSpecifications {

    public static Specification<Transaction> accountOwnerId(UUID ownerId) {
        return (root, query, criteriaBuilder) -> {
            Join<Transaction, Account> accountJoin = root.join("account", JoinType.INNER);
            Join<Account, User> userJoin = accountJoin.join("owners", JoinType.INNER);
            return criteriaBuilder.equal(userJoin.get("id"), ownerId);
        };
    }

    public static Specification<Transaction> withFilter(TransactionFilter filter) {
        return accountIn(filter.getAccounts())
                .and(includeTransfers(filter.isIncludeTransfers()))
                .and(dateBetween(filter.getDateFrom(), filter.getDateTo()));
    }

    public static Specification<Transaction> accountIn(Collection<UUID> accountIds) {
        return (root, query, criteriaBuilder) -> {
            if (accountIds == null) return criteriaBuilder.conjunction();
            else return root.get("account").get("id").in(accountIds);
        };
    }

    public static Specification<Transaction> includeTransfers(boolean includeTransfers) {
        return (root, query, criteriaBuilder) -> {
            if (includeTransfers) return criteriaBuilder.conjunction();
            else return root.get("type").in(TransactionType.TRANSFER_IN, TransactionType.TRANSFER_OUT).not();
        };
    }

    public static Specification<Transaction> dateBetween(LocalDate from, LocalDate to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null || to == null) return criteriaBuilder.conjunction();
            else return criteriaBuilder.between(root.get("transactionDate"), from, to);
        };
    }
}
