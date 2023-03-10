#!/bin/sh
# Ubuntu 14
mysqlCount=`which mysql|wc -l`
if [ $mysqlCount -eq 1 ]
then
    echo "mysql -h 127.0.0.1 -u root -proot "
    exit 0
fi

# Mac
mysqlshCount=`which mysqlsh|wc -l`
if [ $mysqlshCount -eq 1 ]
then
    echo "mysqlsh --sql -h 127.0.0.1 -u root "
    exit 0
fi