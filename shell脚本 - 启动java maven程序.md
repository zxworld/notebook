### shell脚本 - 启动java maven程序

```java
#!/bin/bash

project_name="soulfire-user-center"
project_web_name="soulfire-user-center-web"
jar_name="soulfire-user-center-web-1.0.0.jar"
jenkins_workspace="/var/lib/jenkins/workspace/soulfire-user-center/$project_name"
path="/home/soulfire/code/$project_name"
jar_package="/home/soulfire/jar_package"
web_jar_path="/home/soulfire/code/$project_name/$project_web_name/target/$jar_name"

cp -rf $jenkins_workspace $path
echo "project path is $path"
cd $path
mvn install -Dmaven.test.skip=true
cd $jar_package
mkdir $project_name
cd $project_name
current_path=`pwd`
echo "current patht is $current_path"
cp -rf $web_jar_path $current_path
process=`ps -ef|grep $jar_name|grep -v grep|awk '{print $2}'`
echo "process is $process"
if [ -n "$process" ];then
  echo "kill $process"
  kill -9 $process
  echo "kill result: $?"
fi
`java -jar $jar_name >> log.out &`
newPID=`ps -ef|grep $jar_name|grep -v grep|awk '{print $2}'`
echo "new pid is $newPID"
echo "process end"
exit 0
``` 