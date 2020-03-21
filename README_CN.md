# 语言

[英文页面](README.md)

# What

PrettyZoo 是一个基于 Apache Curator 和 JavaFX 实现的 Zookeeper 图形化管理客户端。

基于 JDK11 实现，采用了 Java 最新的模块化打包技术，无需安装 Java 运行环境即可运行。

点击进入 [下载地址](https://github.com/vran-dev/PrettyZoo/releases)

# 规划

1、支持 SSH tunnel

2、支持配置导入，导出

3、支持国际化

# 特性

1、可同时管理多个 zookeeper 连接

2、节点数据实时同步

3、支持递归和非递归模式操作（默认非递归模式，只能删除或增加无子节点的节点）

4、支持节点搜索，高亮

5、支持简单的 ACL，以及 ACL 语法检查

# 界面展示

![](release/img/main-view.jpg)

![](release/img/add-server.png)

![](release/img/add-node.png)

![](release/img/search-view.jpg)


# 代码架构图


![prettyzoo-arch](release/img/prettyzoo-arch.jpg)

![image-20191230163721866](release/img/prettyzoo-arch2.png)