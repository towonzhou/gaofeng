#!/bin/bash

OUTPUT="/tmp/create_table.sql"

echo "create database if not exists gaofeng;" > $OUTPUT

cat << EOF >> $OUTPUT
CREATE TABLE gaofeng.drivers (
  id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  password varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  phone int(11) DEFAULT '0',
  PRIMARY KEY (id)
) 
EOF

mysql -u root -p < $OUTPUT
