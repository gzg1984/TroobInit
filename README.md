# Ubuntu22
## Prepare
```shell
sudo apt install -y maven java
```
## 测试命令
```shell
java -jar target/index-0.0.3-SNAPSHOT.jar -r /home/zhiganggao -p test
# 会分析/home/zhiganggao/test
# 索引会产生在 /home/zhiganggao/Index

```

# Ubuntu 14 
## 全自动创建索引，并插入两张表
```shell
./auto.sh /opt/project_base/linux-1.0
```
## 单纯创建索引
```shell
mvn package 

# ubuntu14 测试命令
java -jar target/index-0.0.3-SNAPSHOT.jar -r /root/IndexTest -p testSourceFolder

```

# Mac
## 全自动创建索引，并插入两张表
```shell
sudo ./auto.sh /Users/gaozhigang/Downloads/cnlxr_test1
```
## 单纯创建索引
```shell
mvn package -q  
java -jar target/index-0.0.3-SNAPSHOT.jar -r /Users/gaozhigang/Downloads -p test

```
