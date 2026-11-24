#!/usr/bin/env bash
set -Eeuo pipefail

# WatchNext - inicializacion de MySQL (igual que en PagoFlex).
# Crea las bases de cada servicio y el usuario de la app con permisos.
# Se puede correr muchas veces: nunca borra bases, tablas ni datos.
# Las tablas las crea Flyway cuando arranca cada servicio.

: "${DB_ROOT_PASSWORD:?Falta DB_ROOT_PASSWORD}"
: "${DB_USER:?Falta DB_USER}"
: "${DB_PASSWORD:?Falta DB_PASSWORD}"

export MYSQL_PWD="$DB_ROOT_PASSWORD"
DB_HOST="${DB_HOST:-mysql}"
DB_PORT="${DB_PORT:-3306}"

mysql_root() {
    mysql --protocol=tcp --host="$DB_HOST" --port="$DB_PORT" --user=root --batch "$@"
}

# comillas simples escapadas para usar la clave dentro del sql
password_sql="${DB_PASSWORD//\'/\'\'}"

# 1. usuario de la app (si ya existe le sincronizo la clave)
echo "[db-init] usuario ${DB_USER}"
mysql_root -e "CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${password_sql}';
               ALTER USER '${DB_USER}'@'%' IDENTIFIED BY '${password_sql}';"

# 2. una base por microservicio, con permisos solo para la app
for database in auth_db user_db list_db feedback_db news_db; do
    echo "[db-init] base ${database}"
    mysql_root -e "CREATE DATABASE IF NOT EXISTS \`${database}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
                   GRANT ALL PRIVILEGES ON \`${database}\`.* TO '${DB_USER}'@'%';"
done

mysql_root -e "FLUSH PRIVILEGES;"
echo "[db-init] listo"
