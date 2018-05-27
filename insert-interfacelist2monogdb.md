### python 往mongodb写接口信息

> 安装pymongo模块

```
// 查询模块列表
pip3 list（这是3.X版本）

// 安装模块
pip3 install pymongo

```

> 脚本内容

```
__author__ = 'zx'
# -*- coding:utf-8 -*-

from pymongo import MongoClient
from datetime import *
import os

"""
往mongodb里面写入接口信息
"""


class MongodbConn:
    conn = ''
    def_port = 20001
    db = ''

    def __init__(self, host, port=None, username=None, password=None):
        if host is None or not host:
            raise Exception('host most not be empty.')
        if port is None or not port:
            port = self.def_port

        self.conn = MongoClient(str(host), int(port), username=username, password=password)
        pass

    def getConn(self):
        return self.conn

    def getDb(self, dataBaseName):
        if not dataBaseName:
            raise Exception('dataBaseName most not be empty.')
        self.db = dataBaseName
        return self.conn.get_database(dataBaseName)

    def getCollection(self, dataBaseName, collectionName):
        if not collectionName:
            raise Exception('collectionName most not be empty.')
        return self.getDb(dataBaseName).get_collection(collectionName)


class InterfaceInfo:
    name = ''
    addr = ''
    remark = ''
    createTime = ''
    createUser = ''

    def __init__(self, name, addr, remark, createTime=None, createUser=None):
        if not name or not addr:
            raise Exception('name and addr most not be empty.')
        self.name = name
        self.addr = addr
        self.remark = remark
        if createTime is None or not createTime:
            self.createTime = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        else:
            self.createTime = createTime
        if createUser is None or not createUser:
            self.createUser = '脚本'
        else:
            self.createUser = createUser
        pass


class ReadFile:
    filePath = ''

    def __init__(self, filePath):
        if not filePath:
            raise Exception('file path most not be empty.')
        exits = os.path.exists(filePath)
        if exits is False:
            raise Exception('file path error.please check your path.')
        self.filePath = filePath
        pass

    def getStrDict(self):
        di = {}
        for line in open(self.filePath):
            if line.startswith("#"):
                continue
            else:
                value = line.strip().split("=")
                di[value[0]] = value[1]
        return di


if __name__ == '__main__':
    """
    取得mongodb链接
    """
    m = MongodbConn('10.4.68.61')
    conn = m.getConn()
    collection = m.getCollection('mdm_base', 'interface_info')

    """
    读取接口文件为一个字典
    好处在于重复的key 以最后一个的value为准
    """
    f = ReadFile('/Users/zx/Downloads/interface-list.properties')
    str_dict = f.getStrDict()

    """
    将数据转换成一个对象dict(字典)
    """
    infos = []
    for name, addr in str_dict.items():
        print(name, addr)
        infos.append(InterfaceInfo(name, addr, '脚本添加').__dict__)

    """
    批量添加数据
    """
    # collection.insert_many(infos)

    """
    删除collection里面的所有数据
    """
    # collection.remove()

    """
    查询
    """
    for i in collection.find():
        print(i)

```