# JustItemClear

JustItemClear是一个轻量级的Minecraft Bukkit/Spigot插件，用于自动清理服务器中的掉落物。

## 功能特点

- 定时自动清理所有世界中的掉落物
- 支持多种提示方式：聊天消息、ActionBar和BossBar
- 可自定义清理间隔和倒计时提醒
- 提供详细的日志输出，记录清理统计信息
- 支持颜色代码的消息配置

## 安装方法

1. 确保您的Minecraft服务器已安装Bukkit或Spigot的1.16+的版本
2. 下载`justitemclear-1.0-SNAPSHOT.jar`文件
3. 将JAR文件放入服务器的`plugins`文件夹中
4. 重启服务器，插件会自动生成配置文件
5. 根据需要编辑`plugins/JustItemClear/config.yml`文件

## 配置选项

插件的配置文件`config.yml`包含以下选项：

## 命令说明

目前插件暂未提供玩家命令，所有功能通过配置文件设置后自动运行。

## 权限设置

插件无需特殊权限，安装后会自动运行。

## 开发说明

如果您想参与插件开发或进行自定义修改：

1. 克隆本仓库
2. 使用Maven构建项目：`mvn clean install`
3. 生成的JAR文件位于`target`目录中

## 依赖项

- Minecraft Server (Bukkit/Spigot 1.16+)
- Java 8或更高版本
- Maven

## 版本更新日志

### 版本 1.0.0
- 初始版本发布
- 支持定时自动清理掉落物
- 支持聊天、ActionBar和BossBar三种提示方式
- 提供可配置的倒计时提醒
- 添加详细的日志记录功能

## 许可协议

本插件采用GPL 3.0许可协议，详情请参阅LICENSE文件。

## 联系方式

如有问题或建议，请联系插件开发者或创建Issus。
Email: hlpeme1@outlook.com
QQ: 2028356250@qq.com
