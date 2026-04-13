#!/usr/bin/env bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
SELECT format('CREATE DATABASE %I', 'audit_projection')
WHERE NOT EXISTS (
  SELECT 1 FROM pg_database WHERE datname = 'audit_projection'
)\gexec
SQL
