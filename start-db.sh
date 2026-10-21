#!/bin/bash
echo "Starting Alpine MySQL/MariaDB container..."
docker-compose up -d
echo "Database is running on localhost:3306 (database: perpustakaan, user: root, password: <empty>)"
