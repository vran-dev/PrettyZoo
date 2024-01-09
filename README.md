<p align="center">
    <img src="release/img/icon.png" width="200">
</p>


![release-version](https://img.shields.io/github/v/release/vran-dev/prettyZoo?include_prereleases&style=for-the-badge) ![downloads](https://img.shields.io/github/downloads/vran-dev/PrettyZoo/total?style=for-the-badge) ![language](https://img.shields.io/github/languages/top/vran-dev/PrettyZoo?style=for-the-badge) ![licence](https://img.shields.io/github/license/vran-dev/PrettyZoo?style=for-the-badge) ![stars](https://img.shields.io/github/stars/vran-dev/PrettyZoo?style=for-the-badge)

#  Announce

Hello PrettyZoo users.

Since the first version was released on September 30, 2019, almost 4 years have passed. I would like to take this opportunity to express my deepest gratitude to each and every one of you users for being with and supporting this project throughout this time.

However, due to time and energy constraints, I have had to decide to stop maintaining the project. This is a difficult decision, but I believe it is the best choice for the current situation, and such limitations prevent me from continuing to fully support and update the project.

Over the years, we have seen the project grow and develop together. I value the feedback and suggestions that every user has given us, and your support is what keeps me going. However, I have to be honest and face the reality that I can no longer guarantee that the project will be continuously updated and maintained.

Although I will no longer be maintaining the project, I hope that this open source project will continue to bring value to all of you. After the project is archived, you can still develop and iterate on the project by forking it, and interested developers and users are encouraged to develop and contribute in their own way, so that the project can continue in another form of interest.

Thanks again to everyone for their support and understanding, and apologies to anyone who had expectations for the project!

2024-01-09 22:15 by vran


# Language

English |  [中文](README_CN.md)

# What

[PrettyZoo](https://github.com/vran-dev/PrettyZoo) is a GUI for [Zookeeper](https://zookeeper.apache.org/) created by
JavaFX and Apache Curator Framework.

You can download and install at [Release](https://github.com/vran-dev/PrettyZoo/releases), support

- Windows (msi)
- Mac (dmg)
- Linux (rpm & deb)

## If you see PrettyZoo is damaged  in Mac

you can see the solution in [issue-219](https://github.com/vran-dev/PrettyZoo/issues/219)

1. run the follow command

```shell
sudo spctl --master-disable
```

2. open System Preferences->Security & Privacy, select **anywhere**
3. run the follow command

```shell
xattr -rc /Applications/prettyZoo.app
```

4. Enjoy it

# TODO

1.
    - [x] Support i18n (V1.9.0+)
2.
    - [ ] terminal highlight
3.
    - [x] global font size change (v1.6.0+)
4.
    - [x] node data highlight (V1.7.0+)
5.
    - [x] migration UI library to   [Jfoenix](https://github.com/sshahine/JFoenix) ( V1.8.0+)
6.
    - [ ] zookeeper monitor
7.
    - [x] log dashboard (v1.9.3)

# Feature

1. Multi zookeeper server manage
2. Support real-time node synchronize
3. Support ACL
4. Support SSH tunnel
5. Support config export / import
6. Support node create / search / update / delete
7. Support terminal operation
8. Support **JSON** / **XML** data pretty format
9. Support node data hightlight ( Json / Xml / Properties )
10. Support reconnet zookeeper automatic

## Build

See wiki: [build yourself](https://github.com/vran-dev/PrettyZoo/wiki/build-yourself)

# Sponsor

By wechat sponsor code

<img src="release/img/sponsor.jpg" width="250px"/>

# Show

![new-ui.gif](https://s2.loli.net/2022/11/20/hIwX7MQDSbVqk52.gif)

![dark.gif](https://s2.loli.net/2022/11/20/8Yh6TjcfU5Fzy7b.gif)

![timeout.gif](https://s2.loli.net/2022/11/20/CTFNVoWAUalKIzk.gif)

## Thanks

- [ZooKeeper GUI 客户端](http://vip.iocoder.cn/Zookeeper/PrettyZoo/)  by 「芋道源码」

- [PrettyZoo, 颜值与功能双在线的 Zookeeper 可视化工具](https://mp.weixin.qq.com/s/TkFirILto_moEv_kjBBPFw)

# Supported by

[Jetbrains](https://www.jetbrains.com/)
![https://www.jetbrains.com/](release/img/jetbrains.svg)