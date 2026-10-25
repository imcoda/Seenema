# Seenema

轻量化观影记录，基于 [Sitavi/Seenema-movie-tracker](https://github.com/Sitavi/Seenema-movie-tracker) 修改的 Android 个人版。

## 功能

- 搜索电影及官方海报（IMDb），也可手动添加、替换海报。
- 按观看日期分组的时间线和海报墙。
- 观看日期、可选评分、个人笔记、本地剧照及大图浏览。
- 记录和图片保存在手机；完整 ZIP 备份与合并导入，兼容原版 CSV。
- 已移除待看、人物/演职员页面和预告片播放器。

已有记录、缓存海报和剧照可离线查看；搜索与电影资料需要联网。导入保留已有记录，只添加新记录。单张图片上限 20 MB、4000 万像素，每部电影最多 20 张；完整备份上限 512 MB。

## 构建

需要 JDK 17 或 21、Android SDK Platform 36.1，以及 Build Tools 35.0.0。

```sh
./gradlew assembleDebug testDebugUnitTest
```

Windows 使用 `gradlew.bat`。APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。调试包可直接安装，包名为 `ink.c0d4.seenema`，支持 Android 8.0 以上，可与官方版并存。调试签名只适合个人试用；后续更新应持续使用同一签名，卸载前请导出完整备份。

未设置 `SEENEMA_ARTIFACT_RELAY` 时只使用 Google Maven、Maven Central 和 Gradle Plugin Portal。该可选环境变量供本机构建时转发公开依赖，不涉及账号或用户数据。

## 数据与备份

从右上菜单导出完整备份，再通过系统分享面板保存 ZIP；恢复时选择导入完整备份。备份包含 CSV 记录、剧照、缓存海报和图片索引。旧 CSV 不包含图片。剧照会复制到应用目录，原相册图片移动或删除不影响副本；卸载应用会删除应用图片。

## 来源与许可

上游作者：Sitavi，Copyright (C) 2026。基于上游提交 `03976ec` 修改。
保留上游图标、主题、基础组件与 IMDb 客户端，修改记录界面和本地数据管理。
本项目按 GPL-3.0-or-later 分发，见 [LICENSE](LICENSE)。本仓库不是上游官方发布。
