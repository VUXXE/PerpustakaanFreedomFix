FROM alpine:latest

# Install MariaDB server & client, and tzdata
RUN apk add --no-cache \
    mariadb \
    mariadb-client \
    mariadb-server-utils \
    tzdata

# Create directories and fix permissions
RUN mkdir -p /run/mysqld /docker-entrypoint-initdb.d /var/lib/mysql && \
    chown -R mysql:mysql /run/mysqld /var/lib/mysql

# Configure MariaDB server to allow TCP network connections on port 3306
COPY docker/my.cnf /etc/my.cnf.d/mariadb-server.cnf

COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

COPY docker/init.sql /docker-entrypoint-initdb.d/00_init.sql
COPY dummy_data.sql /docker-entrypoint-initdb.d/01_dummy_data.sql

EXPOSE 3306
VOLUME ["/var/lib/mysql"]

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
