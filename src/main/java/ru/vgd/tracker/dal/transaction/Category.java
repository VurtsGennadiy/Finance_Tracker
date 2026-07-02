package ru.vgd.tracker.dal.transaction;

import lombok.Getter;

import java.util.List;

/**
 * Категории операций
 */
@Getter
public enum Category {
    INCOME_SALARY("Зарплата"),
    INCOME_FREELANCE("Фриланс"),
    INCOME_INVESTMENT("Инвестиции"),
    INCOME_GIFT("Подарок"),
    INCOME_SALE("Продажа"),
    INCOME_LOAN("Займ"),
    INCOME_CASHBACK("Кэшбэк"),
    INCOME_OTHER("Другой доход"),

    EXPENSE_PRODUCT("Продукты"),
    EXPENSE_RESTAURANT("Кафе и рестораны"),
    EXPENSE_TRANSPORT("Транспорт"),
    EXPENSE_AUTO("Содержание авто"),
    EXPENSE_HOME("Содержание жилья"),
    EXPENSE_DEBT("Обслуживание долга"),
    EXPENSE_HEALTH("Здоровье"),
    EXPENSE_CLOTH("Одежда"),
    EXPENSE_ENTERTAINMENT("Развлечения"),
    EXPENSE_EDUCATION("Образование"),
    EXPENSE_GIFT("Подарок"),
    EXPENSE_OTHER("Другие расходы");

    private final String displayName;


    Category(String displayName) {
        this.displayName = displayName;
    }

    public static List<Category> getIncomeCategories() {
        return List.of(
                INCOME_SALARY,
                INCOME_FREELANCE,
                INCOME_INVESTMENT,
                INCOME_GIFT,
                INCOME_SALE,
                INCOME_OTHER
        );
    }

    public static List<Category> getExpenseCategories() {
        return List.of(
                EXPENSE_PRODUCT,
                EXPENSE_RESTAURANT,
                EXPENSE_TRANSPORT,
                EXPENSE_AUTO,
                EXPENSE_HOME,
                EXPENSE_DEBT,
                EXPENSE_HEALTH,
                EXPENSE_CLOTH,
                EXPENSE_ENTERTAINMENT,
                EXPENSE_EDUCATION,
                EXPENSE_OTHER
        );
    }

    public boolean isIncomeCategory() {
        return this.name().startsWith("INCOME_");
    }

    public boolean isExpenseCategory() {
        return this.name().startsWith("EXPENSE_");
    }
}
