#!/usr/bin/env bash
#
# Деплой приложения на сервере.
# Запускается из CI по SSH (или вручную) в каталоге, где лежат:
#   app.jar, Dockerfile, docker-compose.yaml, .env, remote-deploy.sh
#
# Переменные окружения:
#   APP_TAG   — тег собираемого образа (по умолчанию latest, CI передаёт SHA коммита)
#   APP_URL   — URL для проверки готовности (по умолчанию http://localhost:8080/login)
#
# Скрипт идемпотентен: его можно запускать повторно.

set -euo pipefail

APP_TAG="${APP_TAG:-latest}"
APP_IMAGE="walletcontrol:${APP_TAG}"
APP_CONTAINER="wallet_app"
APP_URL="${APP_URL:-http://localhost:8080/login}"
HEALTH_ATTEMPTS="${HEALTH_ATTEMPTS:-45}"
HEALTH_DELAY="${HEALTH_DELAY:-2}"
BACKUPS_TO_KEEP=10

cd "$(dirname "$0")"

log() { printf '\n== %s ==\n' "$1"; }
die() { printf 'ОШИБКА: %s\n' "$1" >&2; exit 1; }

log "Проверка файлов деплоя"
for file in Dockerfile docker-compose.yaml app.jar; do
    [ -f "$file" ] || die "не найден файл $file"
done
docker compose version >/dev/null 2>&1 || die "не найден docker compose (нужен Docker Compose v2)"
command -v curl >/dev/null 2>&1 || die "на сервере не найден curl — он нужен для проверки готовности приложения"

log "Резервная копия базы данных"
db_container="$(docker compose ps -q db 2>/dev/null || true)"
if [ -n "$db_container" ]; then
    mkdir -p backups
    backup_file="backups/wallet_$(date +%Y%m%d-%H%M%S).sql"
    # POSTGRES_USER и POSTGRES_DB берутся из окружения самого контейнера
    docker exec "$db_container" sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > "$backup_file" \
        || die "не удалось создать резервную копию БД"
    [ -s "$backup_file" ] || die "резервная копия БД пуста: $backup_file"
    echo "Сохранено: $backup_file ($(du -h "$backup_file" | cut -f1))"
    # Оставляем только последние BACKUPS_TO_KEEP копий
    ls -1t backups/*.sql 2>/dev/null | tail -n +$((BACKUPS_TO_KEEP + 1)) | xargs -r rm -f
else
    echo "Контейнер БД не запущен — резервная копия пропущена (первый деплой?)"
fi

log "Сборка образа $APP_IMAGE"
# Текущий образ сохраняем как previous — на случай неудачного деплоя
previous_available=0
if docker image inspect walletcontrol:latest >/dev/null 2>&1; then
    docker tag walletcontrol:latest walletcontrol:previous
    previous_available=1
    echo "Предыдущий образ сохранён как walletcontrol:previous"
fi

docker build --build-arg JAR_FILE=app.jar -t "$APP_IMAGE" -t walletcontrol:latest .
echo "Собран образ $APP_IMAGE"

log "Запуск контейнеров"
if ! APP_TAG="$APP_TAG" docker compose up -d --remove-orphans; then
    if [ "$previous_available" -eq 1 ]; then
        echo "Не удалось запустить новую версию — откат на предыдущую..." >&2
        docker tag walletcontrol:previous walletcontrol:latest
        APP_TAG=previous docker compose up -d || true
    fi
    die "деплой не удался: не удалось запустить контейнеры приложения"
fi

log "Ожидание готовности приложения"
ready=0
for _ in $(seq 1 "$HEALTH_ATTEMPTS"); do
    if curl -fsS -o /dev/null --max-time 5 "$APP_URL"; then
        ready=1
        break
    fi
    sleep "$HEALTH_DELAY"
done

if [ "$ready" -ne 1 ]; then
    echo "Приложение не ответило по адресу $APP_URL. Последние строки лога:" >&2
    docker compose logs --tail=100 app >&2 || true
    if [ "$previous_available" -eq 1 ]; then
        echo "Откат на предыдущую версию..." >&2
        docker tag walletcontrol:previous walletcontrol:latest
        APP_TAG=previous docker compose up -d
        die "деплой не удался: приложение не прошло проверку готовности, выполнен откат на предыдущую версию"
    fi
    die "деплой не удался: приложение не прошло проверку готовности"
fi
echo "Приложение отвечает: $APP_URL"

log "Очистка неиспользуемых образов"
docker image prune -f >/dev/null || true

log "Готово"
echo "Задеплоен образ $APP_IMAGE, контейнер $APP_CONTAINER"
