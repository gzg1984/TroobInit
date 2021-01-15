#!/bin/sh



#update lxr_db.tb_project_file set file_path='/test/curl.sh' where uuid='testuuid' ;


TARGETROOT=/opt
#TARGETROOT=/opt/file_root
TARGETPROJECTROOT=${TARGETROOT}/project_base
TARGETINDEXROOT=${TARGETROOT}/index_base

PROJECT=$1
if [ "$PROJECT" = "" ]
then
    echo "Need Project Full Path"
    exit 255
fi


if [ ! -d "$PROJECT" ]
then
    echo "Project Full Path should be a Directory"
    exit 254
fi



#echo $PROJECT
projectName=$(basename $PROJECT)
#echo $projectBase
projectFolder=$(dirname $PROJECT)
#echo $projectFolder


# cd /Library/tomcat/bin
# sudo ./shutdown.sh
mysqlsh --sql -h 127.0.0.1 -u root  <<!
insert into lxr_db.tb_project_base (title,\`desc\`,source_from,download_count,visit_count,is_show,add_time,index_path,zip_url,title_en,status) 
values ('$projectName','$projectName','$projectName',0,0,1,now(),'$projectName','$projectName','$projectName',0);
!

mysqlsh --sql -h 127.0.0.1 -u root  <<! > temp.log
select project_id from  lxr_db.tb_project_base where title='$projectName';
!
# For test , you need
#mysqlsh --sql -h 127.0.0.1 -u root  <<!
#delete from lxr_db.tb_project_base where title='$projectName';
#!

 
PROJECTID=`grep -v project_id temp.log`

echo "Now $projectName has ID $PROJECTID"

OLDPWD=`pwd`
cd $projectFolder
find $projectName > tempfile.log
UUIDTEMPCOUNT=0
for i in `cat tempfile.log` 
do
    UUIDTEMPCOUNT=`expr $UUIDTEMPCOUNT + 1 `
    if [ -d $i ]
    then
        filename=$(basename $i)
        echo "insert into lxr_db.tb_project_file (\`uuid\`,\`parent_uuid\`,\`file_path\`,\`file_type\`,\`file_size\`,\`project_id\`,\`file_name\`,\`visit_count\`,\`last_comment_time\`,\`comment_count\`) values('$projectName$UUIDTEMPCOUNT','','/$i',2,NULL,$PROJECTID,'$filename',0,NULL,0);"
        continue
    fi
    if [ -f $i ]
    then
        filesize=`ls -l $i|awk '{print $5}'`
        filename=$(basename $i)
        echo "insert into lxr_db.tb_project_file (\`uuid\`,\`parent_uuid\`,\`file_path\`,\`file_type\`,\`file_size\`,\`project_id\`,\`file_name\`,\`visit_count\`,\`last_comment_time\`,\`comment_count\`) values('$projectName$UUIDTEMPCOUNT','','/$i',1,$filesize,$PROJECTID,'$filename',0,NULL,0);"
        continue
    fi

done >tempInsert.log

mysqlsh --sql -h 127.0.0.1 -u root < tempInsert.log
#echo $OLDPWD
cd $OLDPWD
rm -rf $projectFolder/index
java -jar target/index-0.0.3-SNAPSHOT.jar -r $projectFolder -p $projectName
cp -rf $projectFolder/$projectName $TARGETPROJECTROOT/
cp -rf $projectFolder/Index $TARGETINDEXROOT/$projectName
# cd /Library/tomcat/bin
# sudo ./startup.sh
