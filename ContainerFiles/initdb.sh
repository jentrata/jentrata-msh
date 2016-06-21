#!/bin/bash -ex

psql --username postgres <<-EOSQL
    CREATE USER "$DB_USER_NAME" WITH SUPERUSER PASSWORD '$DB_USER_PASS' ;
EOSQL

gosu postgres createdb -O "${DB_USER_NAME}" ebms
gosu postgres createdb -O "${DB_USER_NAME}" as2

gosu postgres psql -f /work/sql/ebms.sql ebms
gosu postgres psql -f /work/sql/as2.sql as2
