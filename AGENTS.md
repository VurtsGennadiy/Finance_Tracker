# agents.md — правила работы с проектом Finance_Tracker

Файл описывает соглашения проекта и обязательные ограничения для людей и AI-агентов,
которые вносят изменения в кодовую базу.

> **Главное правило: файлы миграций изменять запрещено.**
> Схема БД меняется только новыми changeset'ами (см. раздел «Запрет на изменение миграций»).

---

## 1. О проекте

Трекер личных финансов: счета (наличные, банковские, дебетовые и кредитные карты, кредиты),
доходы/расходы, переводы между счетами, категории, совместные счета.

Технологический стек:

| Компонент | Версия / детали |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.0 (`spring.application.name: Wallet_Control`) |
| Сборка | Maven Wrapper (`mvnw`, `mvnw.cmd`) |
| БД | PostgreSQL 18 (Docker, `docker-compose.yaml`) |
| Миграции | Liquibase, XML-формат, `spring.liquibase.change-log: db/changelog/db.changelog-master.xml` |
| ORM | Spring Data JPA / Hibernate, `ddl-auto: none` |
| Безопасность | Spring Security, `SecurityUser` / `JpaUserDetailsService` |
| Шаблоны | Thymeleaf + layout-dialect, Bootstrap 5 (webjars) |
| Маппинг | MapStruct 1.6.3 |
| Прочее | Lombok, Zalando Logbook (логирование HTTP), Spring Mail, Actuator |
| Тесты | JUnit 5, AssertJ, Testcontainers (`postgres:18`) |

Профили: базовый `application.yaml`, `dev` (локальная БД `wallet_dev`, подробные логи),
`prod` (переменные окружения, Docker).

---

## 2. Структура проекта

```
src/main/java/ru/vgd/tracker/
├── web/              контроллеры (MVC, Thymeleaf)
├── facade/           сборка ответов для страниц (MainFacade, TransactionFacade)
├── service/          бизнес-логика: интерфейсы, impl/, dto/
├── dal/              сущности JPA и репозитории (user, account, transaction, token)
├── security/         SecurityConfig, SecurityService, SecurityUser
├── config/           AsyncConfig, SchedulerConfig
├── util/mapper/      MapStruct-мапперы
├── exception/        доменные исключения
└── dev/              dev-профиль: DevSecurityConfig, DevAuthenticationFilter, DataSeeder

src/main/resources/
├── db/changelog/     миграции Liquibase (только добавление новых файлов!)
├── templates/        Thymeleaf-шаблоны
├── static/           css, js, favicon
└── application*.yaml конфигурация профилей

src/test/java/ru/vgd/tracker/   тесты (*Test — юнит, *IT — интеграционные)
src/test/resources/sql/         SQL-скрипты для подготовки/очистки данных тестов
```

Каталоги `volumes/` (данные PostgreSQL), `logs/`, `target/`, `.idea/` — служебные,
их не нужно править или коммитить.

---

## 3. Запрет на изменение миграций

**Нельзя изменять, удалять, переименовывать, переформатировать или «исправлять» существующие
файлы миграций.** Это касается всего содержимого `src/main/resources/db/changelog/**`:

- `db.changelog-master.xml` и файлы `changelog-v.0.0.N.xml`;
- любые файлы changeset'ов внутри `v.0.0.N/`;
- уже применённые `<changeSet>` — включая id, author, `<comment>`, SQL и отступы.

Причина: Liquibase хранит контрольные суммы применённых changeset'ов в таблицах
`DATABASECHANGELOG` / `DATABASECHANGELOGLOCK`. Любое изменение уже применённого файла ломает
запуск приложения на всех существующих БД (checksum validation failed) и может привести
к порче данных.

### Что запрещено категорически

1. Править существующий `<changeSet>` (SQL, типы колонок, constraints, порядок операций).
2. Менять `id` или `author` существующего changeset'а.
3. Удалять или переименовывать файлы миграций и папки версий.
4. Менять порядок `<include>` в уже существующих changelog-файлах.
5. Обходить проверку контрольных сумм (`clearCheckSums`, `--changelog-sync`,
   `liquibase.ignore-...`, ручное удаление строк из `DATABASECHANGELOG`).
6. Включать `spring.jpa.hibernate.ddl-auto` в значение, отличное от `none`
   (Hibernate не должен менять схему — только Liquibase).
7. Исполнять миграции напрямую на prod-БД вручную.

### Что разрешено

- Создавать **новые** файлы changeset'ов и новые папки версий.
- **Дополнять** `db.changelog-master.xml` и `changelog-v.0.0.N.xml` новыми строками
  `<include .../>` в конец списка (единственное допустимое изменение существующих файлов;
  удалять и переставлять существующие include нельзя). Если есть сомнения — спросите
  владельца проекта перед изменением.

### Как добавлять изменения схемы

1. Определить версию: новая схема — новая папка `v.0.0.N+1` (текущая версия проекта — `0.0.5`,
   см. `pom.xml`). Согласовать номер с версией приложения.
2. Создать папку `src/main/resources/db/changelog/v.0.0.N/`.
3. Создать агрегирующий файл `changelog-v.0.0.N.xml` с `<databaseChangeLog>` и include'ами
   на файлы изменений (`relativeToChangelogFile="true"`).
4. На каждый логический шаг — отдельный файл с говорящим именем в kebab-case,
   например `create-accounts_credit_card-table.xml`, `add-users-created_at-field.xml`,
   `drop-accounts-card-table.xml`.
5. В файле — один или несколько `<changeSet>`:
   - `id` совпадает с именем файла (или описывает шаг, kebab-case);
   - `author` — `VurtsGD` (как в существующих миграциях);
   - `<comment>` на русском языке: что и зачем меняется;
   - изменения схемы — штатными тегами (`createTable`, `addColumn`, `addForeignKeyConstraint`, ...);
     перенос/правка данных — отдельным changeset'ом с `<sql>`.
6. Добавить `<include file="v.0.0.N/changelog-v.0.0.N.xml"/>` в конец `db.changelog-master.xml`.
7. Проверить миграции на чистой БД (Testcontainers/локальный `wallet_dev`), затем прогнать тесты.
8. Убедиться, что приложение стартует на уже мигрированной БД (повторный запуск не падает).

Пример оформления (стиль проекта):

```xml
<changeSet id="create-accounts_credit_card-table" author="VurtsGD">
    <comment>Создание таблицы для кредитных карт</comment>

    <createTable tableName="accounts_card_credit">
        <column name="account_id" type="uuid">
            <constraints primaryKey="true" primaryKeyName="pk_accounts_card_credit"/>
        </column>
        <!-- ... -->
    </createTable>
</changeSet>
```

---

## 4. Команды

Windows (PowerShell):

```powershell
.\mvnw.cmd clean package                 # сборка + тесты
.\mvnw.cmd test                          # только тесты
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
docker compose up -d db                  # PostgreSQL для локальной разработки
docker compose up -d --build             # приложение целиком (prod-профиль)
```

Linux/macOS — `./mvnw` вместо `.\mvnw.cmd`.

Требования к окружению dev: PostgreSQL на `localhost:5432`, БД `wallet_dev`,
пользователь `postgres`; переменные `MAIL_USERNAME`, `MAIL_PASSWORD` (и `POSTGRES_*`,
`DB_URL`, `ADMIN_*` для Docker).

---

## 5. Соглашения по коду

- **Слои:** контроллер не работает с репозиториями напрямую; бизнес-логика — в `service`,
  агрегация данных для страниц — в `facade`. Зависимости — через конструктор.
- **DTO и сущности:** наружу отдаются DTO (`service/dto`), JPA-сущности не уходят в шаблоны
  и не принимаются из форм. Преобразование — MapStruct-мапперы в `util/mapper`.
- **Деньги:** только `BigDecimal`, масштаб 2 знака; никаких `double`/`float` для сумм.
- **Идентификаторы:** `UUID` (в БД — default `uuidv7()`, см. `set-uuid-v7-default.xml`),
  генерация не дублируется в коде.
- **Транзакции:** операции, меняющие несколько сущностей (перевод между счетами,
  пополнение/списание), помечаются `@Transactional` в сервисном слое.
- **Обработка ошибок:** доменные исключения (`ItemNotFoundException`) + `GlobalControllerAdvice`;
  не глотать исключения и не отдавать стектрейс пользователю вне dev-профиля.
- **Безопасность:** проверка владельца счёта — на стороне сервиса/`SecurityService`,
  не полагаться только на скрытые поля формы. Секреты — только через переменные окружения.
- **Логи:** через SLF4J (`@Slf4j`), без `System.out`, без логирования паролей и токенов.
- **Тексты:** пользовательские сообщения, комментарии в миграциях и документация — на русском.
- **Стиль:** форматирование как в окружающем коде (4 пробела, без «звёздочных» импортов),
  Lombok для бойлерплейта, но без злоупотребления (`@Data` на сущностях не использовать —
  `equals/hashCode` по id).

---

## 6. Тесты

- Юнит-тесты — `*Test`, интеграционные — `*IT` (репозитории, миграции, Testcontainers).
- Для интеграционных тестов БД поднимается через `TestcontainersConfig` (`postgres:18`),
  H2 и in-memory БД не используются.
- Тестовые данные — SQL-скрипты в `src/test/resources/sql/`, после теста данные очищаются.
- Изменение схемы без соответствующего теста/проверки миграции на чистой БД не принимается.

---

## 7. Чек-лист перед завершением задачи

- [ ] Существующие файлы миграций не изменены (`git diff src/main/resources/db/changelog`
      показывает только новые файлы и новые строки `<include>`).
- [ ] Новая миграция проверена на чистой БД, приложение стартует повторно без ошибок checksum.
- [ ] `.\mvnw.cmd test` проходит.
- [ ] Нет секретов, отладочного кода и `System.out` в изменениях.
- [ ] Пользовательские тексты и комментарии — на русском; публичное API/шаблоны не сломаны.
