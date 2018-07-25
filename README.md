# mysql2oracle
源自https://www.linuxidc.com/Linux/2012-06/61667.html <br>

一个多线程并发的java数据拖取工具，在此基础上抽象了一个比较通用的工具，目前是基于JDBC方式，因此支持oracle2oracle，oracle2mysql，mysql2mysql，mysql2oracle，mysql2sqlserver，sqlserver 2mysql等等，通过测试目前有oracle2mysql，mysql2oracle，支持windows和Linux。下面说明下使用方法和测试的用例。

第一步，配置需要同步的数据库和表，打开conf文件下的config.properties，conf文件下包含了其他数据同步的模版，下面举例多线程同步mysql的表到oracle中，config.properties是如何配置的。

#for source database parameters
source.dataSource.initialSize=10
source.dataSource.maxIdle=20
source.dataSource.minIdle=5
source.dataSource.maxActive=100
source.dataSource.maxWait=120000
source.jdbc.driverClassName=com.mysql.jdbc.Driver
source.jdbc.url=jdbc:mysql://10.224.56.188 : 3306/meetingdb?autoReconnect=true&characterEncoding=UTF-8
source.jdbc.username=test
source.jdbc.password=pass
#Target sync data threadNum=source.database.threadNum 
source.database.threadNum=10
source.database.selectSql=select * from  test  where mod(CAST(FNV_64(PATHALIASID) AS UNSIGNED),#threadNum#)=?
#you can input many commands and split by ";" ,你可以打开注释加一些session级别的优化命令
#source.database.sessionCommand=ALTER SESSION SET DB_FILE_MULTIBLOCK_READ_COUNT=128;
#for target jdbc parameters
target.dataSource.initialSize=10
target.dataSource.maxIdle=20
target.dataSource.minIdle=5
target.dataSource.maxActive=100
target.dataSource.maxWait=120000
target.jdbc.driverClassName=oracle.jdbc.driver.OracleDriver
target.jdbc.url=jdbc: oracle: thin : @10.224.56.189:1521:PERFCON1
target.jdbc.username=test
target.jdbc.password=pass
target.database.insertSql=insert into test2(PATHALIASID,PATH,CREATETIME,LASTMODIFIEDTIME,OBJECTPREFIX,PATHMD5ID,COLLIDESWITH) values(?,?,?,?,?,?,?)
target.database.commitNum=1000

第二步，运行dataSync.sh或者dataSync.bat,开始同步数据。


具体的测试的用例如下：


Case 1 : oracle to mysql with multi_threads
source table:test
target table:test2
source.database.selectSql=select * from  test  where ORA_HASH(PATHALIASID,#threadNum#)=?


exeSql=select * from  test where ORA_HASH(PATHALIASID,10)=10
...
exeSql=select * from  test  where ORA_HASH(PATHALIASID,10)=0


mysql> select count(*) from test2;
+----------+
| count(*) |
+----------+
|    218850 |
+----------+

Case 2 : mysql to oracle with multi_threads
source table:test
target table:test2
source.database.selectSql=select * from  test where mod(CAST(FNV_64(PATHALIASID) AS UNSIGNED),#threadNum#)=?

 

exeSql=select * from  test where mod(CAST(FNV_64(PATHALIASID) AS UNSIGNED),10)=10
...
exeSql=select * from  test where mod(CAST(FNV_64(PATHALIASID) AS UNSIGNED),10)=0
SQL>select count(*) from test2;
  COUNT(*)
----------
     218850
  
case 3 :mysql to oracle with single process.
source.database.threadNum=1
source.database.selectSql=select * from  test
exeSql=select * from  test

SQL>select count(*) from test2;
  COUNT(*)
----------
     218850

获取可以运行的二进制运行程序或者源码，代码是完全free的，可以从以下地址获取：
由于项目时间过于长,没有git所以上传以防以后找不到.   <br>
dataSyncSingle单线程下更改了几个数据类型.datetime和用于记录金额的decimal类型通过double数据流行保留的小数点后的位数。 <br>
jdbc驱动依赖的版本为oracle11和mysql5.7 <br> 
自己用的运行方式是打包成jar包,java -jar aa.jar /config/
