#!/bin/sh
. projectFile.sh

#update lxr_db.tb_project_file set file_path='/test/curl.sh' where uuid='testuuid' ;
TARGETROOT=/opt
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

MYSQLENTRY=`./getmysql.sh`
#echo $PROJECT
projectName=$(basename $PROJECT)
#echo $projectBase
projectFolder=$(dirname $PROJECT)
#echo $projectFolder


# cd /Library/tomcat/bin
# sudo ./shutdown.sh
$MYSQLENTRY  <<!
delete from lxr_db.tb_project_base where title='$projectName';
insert into lxr_db.tb_project_base (title,\`desc\`,source_from,download_count,visit_count,is_show,add_time,index_path,zip_url,title_en,status)
values ('$projectName','$projectName','$projectName',0,0,1,now(),'$projectName','$projectName','$projectName',0);
!

$MYSQLENTRY <<! > temp.log
select project_id from  lxr_db.tb_project_base where title='$projectName';
!

PROJECTID=`grep -v project_id temp.log`
if [ "$PROJECTID" = "" ]
then
    echo "Cannot get Project ID, quit"
    exit 253
fi
echo "Now $projectName has ID $PROJECTID"

OLDPWD=`pwd`
cd $projectFolder/$projectName



recEchoMysqlCommandForFolder $PROJECT  >/tmp/tempInsert.log


$MYSQLENTRY < /tmp/tempInsert.log
#echo $OLDPWD

echo "Creating Index Folder..."
rm -rf $projectFolder/Index
cd $SHELLPWD
java -jar target/index-0.0.3-SNAPSHOT.jar -r $projectFolder -p $projectName
#java -jar $SHELLPWD/target/index-0.0.3-SNAPSHOT.jar  -p $projectName

echo "Copying Source Folder..."
cd $projectFolder
cp -rf $projectName $TARGETPROJECTROOT/

echo "Copying Index Folder..."
cd $projectFolder
rm -rf $TARGETINDEXROOT/$projectName
cp -rf Index $TARGETINDEXROOT/$projectName

# cd /Library/tomcat/bin
# sudo ./startup.sh
