#!/bin/sh
#
# Load the database schema from sql files in the current directory.
#
# Requires that the database already exists, and requests the database password
# from the user. Has to be run from the directory containing the SQL files.
#
# TODO: Make scriptable by:
#       1. Handling DB creation and dropping existing tables gracefully
#       2. Accept HOST/USER/DB and password passed in
#       3. Do not rely on running in the SQL directory
#
HOST=localhost
USER=anvgl
DB=anvgl

# Disable foreign keys checking so we can load the tables in arbitrary order,
# then re-enable it after all tables are loaded.

mysql -h $HOST -u $USER -p $DB <<EOF
SET foreign_key_checks = 0;

$(for f in *.sql; do echo "source $f ;"; done)

SET foreign_key_checks = 1;
EOF

