#!/bin/sh

# $1 Project root
# $2 relateve path
# $3 Parent UUID
recEchoMysqlCommandForFolder(){
    #echo "calling for $2 at $1 and parent UUID is $3"
    local ParentUUID=$3
    local logFile=/tmp/$ParentUUID.uuid.log
    local folderFullPath=$1/$2
    if [ "$ParentUUID" = "" ]
    then
        logFile=/tmp/root.uuid.log
        folderFullPath=$1
    fi
    #echo "current full path is $folderFullPath"
    ls $folderFullPath > $logFile
    for i in `cat $logFile`
    do
        local relateveFromPWD=$2/$i
        if [ "$2"  = "" ]
        then
            relateveFromPWD=$i
        fi
        local newUUID=`uuidgen`
        #echo "call for $relateveFromPWD new uuid is $newUUID, parent is $ParentUUID"
        if [ -d $relateveFromPWD ]
        then
            filename=$(basename $i)
            echo "insert into lxr_db.tb_project_file (\`uuid\`,\`parent_uuid\`,\`file_path\`,\`file_type\`,\`file_size\`,\`project_id\`,\`file_name\`,\`visit_count\`,\`last_comment_time\`,\`comment_count\`) values('$newUUID','$ParentUUID','/$projectName/$relateveFromPWD',2,NULL,$PROJECTID,'$filename',0,NULL,0);"
            recEchoMysqlCommandForFolder $1 $relateveFromPWD $newUUID
            #echo "Will Rec Call $2/$i in $1"
            continue
        fi
        if [ -f $relateveFromPWD ]
        then
            filesize=`ls -l $relateveFromPWD|awk '{print $5}'`
            filename=$(basename $relateveFromPWD)
            echo "insert into lxr_db.tb_project_file (\`uuid\`,\`parent_uuid\`,\`file_path\`,\`file_type\`,\`file_size\`,\`project_id\`,\`file_name\`,\`visit_count\`,\`last_comment_time\`,\`comment_count\`) values('$newUUID','$ParentUUID','/$projectName/$relateveFromPWD',1,$filesize,$PROJECTID,'$filename',0,NULL,0);"
            continue
        fi
        echo "$i is not directory or file in $PWD"

    done
}
