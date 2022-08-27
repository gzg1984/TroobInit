# Prepare
## Ubuntu22
```
sudo apt install -y maven java
```

# TroobInit
```
mvn package -q  
java -jar target/index-0.0.3-SNAPSHOT.jar -r /Users/gaozhigang/Downloads -p test
java -jar target/index-0.0.3-SNAPSHOT.jar -r /home/zhiganggao -p test
# 会分析/home/zhiganggao/test
# 索引会产生在 /home/zhiganggao/Index
```


# Auto Insert to db
```
sudo ./auto.sh /Users/gaozhigang/Downloads/cnlxr_test1
```