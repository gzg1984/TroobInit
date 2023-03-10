#!/bin/sh

# 用法
# sudo ./auto.sh /Users/gaozhigang/Downloads/cnlxr_test1
# 参数是全路径
# 结果：
. ./projectFile.sh

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

echo "Copying Source Folder... "$projectFolder/$projectName" to "$TARGETPROJECTROOT/$projectName
copyProject $TARGETPROJECTROOT $projectFolder $projectName

 
# Step 1： 将工程相关信息插入到数据库中
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


# Step 2： 将所有文件的信息插入到 tb_project_file 表中
recEchoMysqlCommandForFolder $PROJECT  >/tmp/tempInsert.log


$MYSQLENTRY < /tmp/tempInsert.log
#echo $OLDPWD


# Step 3： 通过本工程下的jar可执行程序，创建索引目录
echo "Creating Index Folder..."
rm -rf $projectFolder/Index
cd $SHELLPWD
java -jar target/index-0.0.3-SNAPSHOT.jar -r $TARGETPROJECTROOT -p $projectName
#java -jar $SHELLPWD/target/index-0.0.3-SNAPSHOT.jar  -p $projectName
#/opt/jdk1.8.0_161/bin/java -jar target/index-0.0.3-SNAPSHOT.jar -r /opt/files/project_base -p TroobInit


# Step 4： 把索引目录拷贝到目标目录下
echo "Copying Index Folder..."
cd $projectFolder
rm -rf $TARGETINDEXROOT/$projectName
cp -rf $TARGETPROJECTROOT/Index $TARGETINDEXROOT/$projectName

# cd /Library/tomcat/bin
# sudo ./startup.sh
