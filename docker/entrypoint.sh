#!/bin/sh
set -e

DATADIR="/var/lib/mysql"
SOCKET="/run/mysqld/mysqld.sock"

mkdir -p "$DATADIR" /run/mysqld
chown -R mysql:mysql "$DATADIR" /run/mysqld

# If the data directory is uninitialized, run bootstrap
if [ ! -d "$DATADIR/mysql" ]; then
    echo "==> [Alpine MariaDB] Initializing database files in $DATADIR..."
    mariadb-install-db --user=mysql --datadir="$DATADIR" --skip-test-db > /dev/null

    echo "==> [Alpine MariaDB] Starting temporary daemon for configuration..."
    mariadbd --user=mysql --datadir="$DATADIR" --skip-networking --socket="$SOCKET" &
    PID="$!"

    # Wait for daemon
    for i in $(seq 1 40); do
        if mariadb-admin --socket="$SOCKET" ping --silent 2>/dev/null; then
            break
        fi
        sleep 0.5
    done

    echo "==> [Alpine MariaDB] Configuring privileges and database '${MYSQL_DATABASE:-perpustakaan}'..."
    mariadb --socket="$SOCKET" <<EOSQL
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE:-perpustakaan}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOSQL

    # Execute initialization SQL files if present
    for f in /docker-entrypoint-initdb.d/*.sql; do
        if [ -f "$f" ]; then
            echo "==> [Alpine MariaDB] Executing: $f"
            mariadb --socket="$SOCKET" "${MYSQL_DATABASE:-perpustakaan}" < "$f" || echo "Notice: Skipped or partial execution for $f"
        fi
    done

    echo "==> [Alpine MariaDB] Shutting down temporary daemon..."
    mariadb-admin --socket="$SOCKET" shutdown
    wait "$PID"
    echo "==> [Alpine MariaDB] Initial bootstrap complete."
fi

echo "==> [Alpine MariaDB] Starting MariaDB server on port 3306..."
exec /usr/bin/mariadbd --user=mysql --datadir="$DATADIR" --console
