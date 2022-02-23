#!/bin/sh
export JRE_HOME=/opt/jdk1.8.0_161/


. ./projectFile.sh

#update lxr_db.tb_project_file set file_path='/test/curl.sh' where uuid='testuuid' ;


#TARGETROOT=/opt
TARGETROOT=/opt/files
#TARGETROOT=/opt/file_root
TARGETPROJECTROOT=${TARGETROOT}/project_base
TARGETINDEXROOT=${TARGETROOT}/index_base
SHELLPWD=$PWD


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


echo "Copying Source Folder..."
cd $projectFolder
rm -rf $TARGETPROJECTROOT/$projectName
cp -rf $projectName $TARGETPROJECTROOT/


# cd /Library/tomcat/bin
# sudo ./shutdown.sh
#mysqlsh --sql -h 127.0.0.1 -u root  <<!
mysql -h 127.0.0.1 -u root -proot lxr_db  <<!
delete from lxr_db.tb_project_base where title='$projectName';
insert into lxr_db.tb_project_base (title,\`desc\`,source_from,download_count,visit_count,is_show,add_time,index_path,zip_url,title_en,status) 
values ('$projectName','$projectName','$projectName',0,0,1,now(),'$projectName','$projectName','$projectName',0);
!


mysql -h 127.0.0.1 -u root -proot lxr_db <<! > temp.log
select project_id from  lxr_db.tb_project_base where title='$projectName';
!


PROJECTID=`grep -v project_id temp.log`

echo "Now $projectName has ID $PROJECTID"

OLDPWD=`pwd`
cd $projectFolder/$projectName


recEchoMysqlCommandForFolder $PROJECT  >/tmp/tempInsert.log

mysql -h 127.0.0.1 -u root -proot lxr_db < /tmp/tempInsert.log


echo "Creating Index Folder..."
rm -rf $projectFolder/Index
cd $SHELLPWD
#java -jar target/index-0.0.3-SNAPSHOT.jar -r $TARGETPROJECTROOT -p $projectName
/opt/jdk1.8.0_161/bin/java -jar target/index-0.0.3-SNAPSHOT.jar -r /opt/files/project_base -p TroobInit


echo "Copying Index Folder..."
cd $projectFolder
rm -rf $TARGETINDEXROOT/$projectName
cp -rf $TARGETPROJECTROOT/Index $TARGETINDEXROOT/$projectName
