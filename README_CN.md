<p align="center">
    <img src="release/img/icon.png">
</p>

![release-version](https://img.shields.io/github/v/release/vran-dev/prettyZoo?include_prereleases&style=for-the-badge) ![downloads](https://img.shields.io/github/downloads/vran-dev/PrettyZoo/total?style=for-the-badge) ![language](https://img.shields.io/github/languages/top/vran-dev/PrettyZoo?style=for-the-badge) ![licence](https://img.shields.io/github/license/vran-dev/PrettyZoo?style=for-the-badge) ![stars](https://img.shields.io/github/stars/vran-dev/PrettyZoo?style=for-the-badge)



# 语言

[English](README.md)  | 中文



# 介绍

[PrettyZoo](https://github.com/vran-dev/PrettyZoo) 是一个基于 Apache Curator 和 JavaFX 实现的 Zookeeper 图形化管理客户端。

使用了 Java 的模块化（Jigsaw）技术，并基于 JPackage 打包了多平台的可运行文件（无需要额外安装 Java 运行时）。

目前已提供了 mac（dmg 文件）、Linux（deb 和 rpm 文件）、windows（msi 文件） 的安装包，[下载地址](https://github.com/vran-dev/PrettyZoo/releases)。

更多内容可以查看：https://mp.weixin.qq.com/s/TkFirILto_moEv_kjBBPFw



# MAC 安装问题

mac 安装提示：已损坏，无法打开

请参考 https://www.macwk.com/article/mac-catalina-1015-file-damage  解决

本质是因为安装包打包时没有加入 apple 平台的签名，我会在后续寻找解决方案



# 规划

1. - [x] 国际化支持（V1.9.0+）
2. - [ ] 命令高亮支持
3. - [x] 全局字体大小配置（v1.6.0+）
4. - [x] 节点数据高亮（V1.7.0+）
5. - [x] 迁移到 [Jfoenix](https://github.com/sshahine/JFoenix)  UI 库 (V1.8.0+)
6. - [ ] Zookeeper 监控
7. - [x] 日志看板



# 特性

1. 可同时管理多个 zookeeper 连接
2. 节点数据实时同步
3. 支持 ACL 配置
4. 支持 SSH Tunnel 连接
5. 支持配置导入、导出
6. 支持节点增删改查操作
7. 支持 command line 操作模式
8. 支持节点数据格式化 JSON、XML
9. 支持节点数据高亮（JSON、XML、Properties）



## 构建

查看 wiki: [build yourself](https://github.com/vran-dev/PrettyZoo/wiki/build-yourself)



# 界面展示



- over view

![](release/img/main.png)



- server info

![](release/img/server.gif)





- node info




![](release/img/time-format.gif)


- node data highlight

![](release/img/highlight.gif)


- node add

![](release/img/create-node.gif)



- node search

![](release/img/search.gif)


- terminal

![](release/img/terminal.gif)

- 4-letter

![](release/img/4-letter.gif)

# 感谢

- 感谢「芋道源码」对 PrettyZoo 的推荐: [ZooKeeper GUI 客户端](http://vip.iocoder.cn/Zookeeper/PrettyZoo/)

-  [PrettyZoo, 颜值与功能双在线的 Zookeeper 可视化工具](https://mp.weixin.qq.com/s/TkFirILto_moEv_kjBBPFw)

