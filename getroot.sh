#!/bin/sh

# example for old troob: ./getroot.sh ~/troob_src_bak/etc

# example for mac: ./getroot.sh /Library/tomcat/webapps

if [ $# -lt 1 ]
then
    echo "need 1 arg"
    exit 255
fi

tryOne=`find $1 -name upload.properties`
for everFile in $tryOne
do
grep "^prefix="  $everFile |sed -e "s/^prefix=//g"
done
