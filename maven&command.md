## 创建web项目

```
mvn archetype:create -DgroupId=com.learn -DartifactId=LearnNew -DarchetypeArtifactId=maven-archetype-webapp
<font color=red>注意：3.0版本以后，用下面的命令</font>
mvn archetype:generate -DgroupId=com.learn -DartifactId=LearnNew -DarchetypeArtifactId=maven-archetype-webapp
```

## 添加jar包到项目

```
mvn install:install-file -DgroupId=com.google.code -DartifactId=kaptcha -Dversion=2.3.2 -Dfile=D:\kaptcha-2.3.jar -Dpackaging=jar -DgeneratePom=true
```

```
使用maven命令在创建项目的时候出现

Generating project in Interactive mode

然后就一直卡住
加个参数 -DarchetypeCatalog=internal
让它不要从远程服务器上取catalog:
```

### 最终的命令

```
mvn archetype:generate -DgroupId=com.cyf -DartifactId=cms -DarchetypeArtifactId=maven-archetype-webapp -DarchetypeCatalog=internal
```
