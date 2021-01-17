#!/bin/sh

if [ "$1" != "force" ]
then
    echo "forbident"
    exit 255
fi

echo "clean all DB data without backup"

DBEntry=`./getmysql.sh`

# This will destory every thing in front page
$DBEntry <<!
use lxr_db;
show tables;
delete from tb_project_base;
delete from tb_project_file;
!