package ru.vgd.tracker.dal.migration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверка миграции v.0.0.6 на базе с уже существующими пользователями и счетами.
 */
class AccountOwnerMigrationIT {

    private static final UUID USER_ID = UUID.fromString("42567893-05e8-4ea2-8d45-6a99941789fb");
    private static final UUID CASH_ACCOUNT_ID = UUID.fromString("5cb417d2-93d8-4a43-b495-f2a5ac44d20b");
    private static final UUID BANK_ACCOUNT_ID = UUID.fromString("886d71ce-de71-4aad-86e4-b8a46f92312d");

    private static PostgreSQLContainer<?> postgres;

    @BeforeAll
    static void startDatabase() {
        postgres = new PostgreSQLContainer<>("postgres:18");
        postgres.start();
    }

    @AfterAll
    static void stopDatabase() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @Test
    @DisplayName("Владелец существующего счёта переносится в accounts_base.owner_id")
    void migrateExistingAccountOwners() throws Exception {
        applyChangelog("db/changelog/test-changelog-up-to-v.0.0.5.xml");
        insertLegacyData();
        applyChangelog("db/changelog/db.changelog-master.xml");
        // Повторный запуск не должен падать из-за контрольных сумм уже применённых changeset'ов.
        applyChangelog("db/changelog/db.changelog-master.xml");

        try (Connection connection = openConnection()) {
            assertOwner(connection, CASH_ACCOUNT_ID);
            assertOwner(connection, BANK_ACCOUNT_ID);
            assertOwnerIdIsNotNull(connection);
            assertAccountOwnersTableDropped(connection);
        }
    }

    private void applyChangelog(String changelog) throws Exception {
        try (Connection connection = openConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(
                    changelog,
                    new ClassLoaderResourceAccessor(),
                    database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }
    }

    private void insertLegacyData() throws SQLException {
        try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO users (user_id, username, email, password, confirmed_email)
                    VALUES ('%s', 'test_user', 'test_user@email.com', 'test_password', true)
                    """.formatted(USER_ID));
            statement.execute("""
                    INSERT INTO accounts_base (account_id, account_type, name, balance)
                    VALUES ('%s', 'CASH', 'Наличные', 1000.00),
                           ('%s', 'BANK', 'Банковский счёт', 2000.00)
                    """.formatted(CASH_ACCOUNT_ID, BANK_ACCOUNT_ID));
            statement.execute("""
                    INSERT INTO accounts_cash (account_id)
                    VALUES ('%s')
                    """.formatted(CASH_ACCOUNT_ID));
            statement.execute("""
                    INSERT INTO accounts_bank (account_id, bank_name)
                    VALUES ('%s', 'Тестовый банк')
                    """.formatted(BANK_ACCOUNT_ID));
            statement.execute("""
                    INSERT INTO account_owners (user_id, account_id)
                    VALUES ('%s', '%s'),
                           ('%s', '%s')
                    """.formatted(USER_ID, CASH_ACCOUNT_ID, USER_ID, BANK_ACCOUNT_ID));
        }
    }

    private void assertOwner(Connection connection, UUID accountId) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT owner_id FROM accounts_base WHERE account_id = '%s'".formatted(accountId))) {
            assertTrue(resultSet.next(), "Счёт должен существовать: " + accountId);
            assertEquals(USER_ID, resultSet.getObject("owner_id", UUID.class));
        }
    }

    private void assertOwnerIdIsNotNull(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("""
                     SELECT is_nullable
                     FROM information_schema.columns
                     WHERE table_schema = 'public'
                       AND table_name = 'accounts_base'
                       AND column_name = 'owner_id'
                     """)) {
            assertTrue(resultSet.next(), "Колонка owner_id должна существовать");
            assertEquals("NO", resultSet.getString("is_nullable"));
        }
    }

    private void assertAccountOwnersTableDropped(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT to_regclass('public.account_owners')")) {
            assertTrue(resultSet.next());
            assertNull(resultSet.getString(1), "Таблица account_owners должна быть удалена");
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }
}
