### 一些常用的linux命令

> less

```
less <file name>
// 跳到最后
esc --> G

// 向上搜索关键字
输入 /<搜索字符>?

```

> 查询程序运行时间

```
ps -eo pid,lstart,etime | grep 31479
```

> 运行jar包程序

```
nohup java -jar xxx.jar &
```

> 查询端口是否开放

```
more /etc/sysconfig/iptables
```

> 查询某个端口状态

```
netstat -ano |grep :8088
```