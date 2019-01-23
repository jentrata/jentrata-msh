#!/bin/bash -ex

psql --username jentrata <<-EOSQL
    CREATE USER "$DB_USER_NAME" WITH SUPERUSER PASSWORD '$DB_USER_PASS' ;
EOSQL

psql --username jentrata <<-EOSQL
    CREATE DATABASE ebms ;
    CREATE DATABASE as2 ;
EOSQL

cat  /work/sql/ebms.sql | psql --username jentrata ebms
cat  /work/sql/as2.sql | psql --username jentrata as2

