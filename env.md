## 使用docker创建一个自己的镜像

* 使用基础镜像

> 本次使用centos作为基础

```
进入官方镜像网站
https://hub.docker.com/explore/
搜索centos
使用命令下载镜像
docker pull centos
```

* 运行镜像

```
查看所有的镜像
docker images

运行centos镜像
docker run -i -t -d centos /bin/bash
```

* 进入容器

```
查看容器
docker ps -a

进入容器
docker exec -it <container ID>
```

* 安装wget下载工具

```
yum list
yum install wget
```

* 下载java安装文件到当前所在目录

```
wget http://download.oracle.com/otn-pub/java/jdk/8u151-b12/e758a0de34e24606bca991d704f6dcbf/jdk-8u151-linux-x64.rpm
该地址为在官网下载文件地址
```

* 安装java

```
rpm -ivh jdk-8u151-linux-x64.rpm
安装完成后,查看版本检查是否安装成功
java -version
```

* 设置java环境变量

```
以rpm包安装方式安装后,文件一般在/usr/java中

cd /usr/java
切换目录,查看版本, 显示如下:
default  jdk1.8.0_151  latest

也可以用以下命令查询:
find -name java
显示如下:
./usr/bin/java
./usr/java
./usr/java/jdk1.8.0_151/jre/bin/java
./usr/java/jdk1.8.0_151/bin/java
./var/lib/alternatives/java
./etc/alternatives/java
./etc/pki/java
./etc/pki/ca-trust/extracted/java

设置全局变量
vi /etc/profile
在文件后面添加如下路径:
#java_env
export JAVA_HOME=/usr/java/jdk1.8.0_151
export JRE_HOME=/usr/java/jdk1.8.0_151/jre
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib export PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin

按ESC
输入:wq将内容写入文件
再检查一下
cat /etc/profile

退出容器
exit
```

* 保存容器的修改

```
停止容器
docker stop <container ID>

保存容器修改为新的镜像
docker commit <container ID> <target name>
如:
docker commit -a 'xx' -m 'add java env' 93e8a5503128 centos/jdk8

OPTIONS:
-a :提交的镜像作者

-c :使用Dockerfile指令来创建镜像

-m :提交时的说明文字

-p :在commit时,将容器暂停
```

* 查看修改的新镜像

```
docker images
```

<table class="table table-bordered table-striped table-condensed">
	<tr>
		<td>REPOSITORY</td>
		<td>TAG</td>
		<td>IMAGE ID</td>
		<td>CREATED</td>
		<td>SIZE</td>
	</tr>
	<tr>
		<td>centos/jdk8</td>
		<td>latest</td>
		<td>3710ac243be6</td>
		<td>2 minutes ago</td>
		<td>818MB</td>
	</tr>
	<tr>
		<td>centos</td>
		<td>latest</td>
		<td>3fa822599e10</td>
		<td>4 days ago</td>
		<td>204MB</td>
	</tr>
</table>

* 查看新镜像详细信息

```
 docker inspect centos/jdk8
```

* 运行保存的新镜像

```
docker run -itd centos/jdk8 /bin/bash

docker exec -it <container ID> /bin/bash

java -verison
java version "1.8.0_151"
Java(TM) SE Runtime Environment (build 1.8.0_151-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.151-b12, mixed mode)

确定为刚才保存的镜像
```

* 安装tomcat

```

```