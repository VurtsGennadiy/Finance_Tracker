INSERT INTO accounts_base (account_id, account_type, "name", balance)
    VALUES ('5cb417d2-93d8-4a43-b495-f2a5ac44d20b', 'CASH', 'cash-account-test', 1000),
    ('886d71ce-de71-4aad-86e4-b8a46f92312d', 'BANK', 'bank-account-test', 2000);

INSERT INTO accounts_cash (account_id)
    VALUES ('5cb417d2-93d8-4a43-b495-f2a5ac44d20b');

INSERT INTO accounts_bank (account_id, bank_name, account_number)
    VALUES('886d71ce-de71-4aad-86e4-b8a46f92312d', 'BANK_NAME', '000 111 222');

INSERT INTO account_owners (user_id, account_id)
    VALUES('42567893-05e8-4ea2-8d45-6a99941789fb', '5cb417d2-93d8-4a43-b495-f2a5ac44d20b'),
    ('42567893-05e8-4ea2-8d45-6a99941789fb', '886d71ce-de71-4aad-86e4-b8a46f92312d');
