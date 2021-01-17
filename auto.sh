#!/bin/sh
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


#echo $PROJECT
projectName=$(basename $PROJECT)
#echo $projectBase
projectFolder=$(dirname $PROJECT)
#echo $projectFolder


# cd /Library/tomcat/bin
# sudo ./shutdown.sh
# TODO: delete all old data in DB
mysqlsh --sql -h 127.0.0.1 -u root  <<!
delete from lxr_db.tb_project_base where title='$projectName';
insert into lxr_db.tb_project_base (title,\`desc\`,source_from,download_count,visit_count,is_show,add_time,index_path,zip_url,title_en,status) 
values ('$projectName','$projectName','$projectName',0,0,1,now(),'$projectName','$projectName','$projectName',0);
!

mysqlsh --sql -h 127.0.0.1 -u root  <<! > temp.log
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

# $1 Project root
# $2 relateve path
# $3 Parent UUID
recEchoMysqlCommandForFolder(){
    #echo "calling for $2 at $1 and parent UUID is $3"
    ParentUUID=$3
    logFile=/tmp/$ParentUUID.uuid.log
    folderFullPath=$1/$2
    if [ "$ParentUUID" = "" ]
    then
        logFile=/tmp/root.uuid.log
        folderFullPath=$1
    fi
    #echo "current full path is $folderFullPath"
    ls $folderFullPath > $logFile
    for i in `cat $logFile` 
    do
        relateveFromPWD=$2/$i
        if [ "$2"  = "" ]
        then
            relateveFromPWD=$i
        fi
        newUUID=`uuidgen`

        if [ -d $relateveFromPWD ]
        then
            filename=$(basename $i)
            echo "insert into lxr_db.tb_project_file (\`uuid\`,\`parent_uuid\`,\`file_path\`,\`file_type\`,\`file_size\`,\`project_id\`,\`file_name\`,\`visit_count\`,\`last_comment_time\`,\`comment_count\`) values('$newUUID','$ParentUUID','/$projectName/$relateveFromPWD',2,NULL,$PROJECTID,'$filename',0,NULL,0);"
            recEchoMysqlCommandForFolder $1 $relateveFromPWD $newUUID
            #echo "Will Rec Call $2/$i in $1"
            continue
        fi
        if [ -f $irelateveFromPWD]
        then
            filesize=`ls -l $relateveFromPWD|awk '{print $5}'`
            filename=$(basename $relateveFromPWD)
            echo "insert into lxr_db.tb_project_file (\`uuid\`,\`parent_uuid\`,\`file_path\`,\`file_type\`,\`file_size\`,\`project_id\`,\`file_name\`,\`visit_count\`,\`last_comment_time\`,\`comment_count\`) values('$newUUID','$ParentUUID','/$projectName/$relateveFromPWD',1,$filesize,$PROJECTID,'$filename',0,NULL,0);"
            continue
        fi
        echo "$i is not directory or file in $PWD"

    done 
}

recEchoMysqlCommandForFolder $PROJECT  >/tmp/tempInsert.log

mysqlsh --sql -h 127.0.0.1 -u root < /tmp/tempInsert.log
#echo $OLDPWD
cd $OLDPWD

echo "Creating Index Folder..."
rm -rf $projectFolder/Index
#java -jar target/index-0.0.3-SNAPSHOT.jar -r $projectFolder -p $projectName
cd $projectFolder
java -jar $SHELLPWD/target/index-0.0.3-SNAPSHOT.jar  -p $projectName

echo "Copying Source Folder..."
cp -rf $projectName $TARGETPROJECTROOT/

echo "Copying Index Folder..."
rm -rf $TARGETINDEXROOT/$projectName
cp -rf Index $TARGETINDEXROOT/$projectName

# cd /Library/tomcat/bin
# sudo ./startup.sh
