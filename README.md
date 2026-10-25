# MovieFlix 个人版

基于 [shalenMathew/MovieFlix_App](https://github.com/shalenMathew/MovieFlix_App) 的 Android 个人观影日记。仓库沿用 `Seenema` 这个名字，当前应用基于 MovieFlix。上游固定提交：`c7ca2763f9dee568f94cbd424983f36cf5ae0ce9`。

保留 MovieFlix 原版的海报卡片、详情页、个人笔记、剧照查看和 ZIP 备份界面。入口精简为“观影 / 搜索 / 设置”；移除推荐首页、待看、追剧、提醒及人物页面入口。

- 按观看月份显示时间线，点击标题可切换海报墙。
- 长按电影卡片编辑观看日期、替换海报或移除记录；详情页也可编辑观看日期。
- 右上角 + 手动添加电影；外页显示海报，详情中记录笔记和上传剧照。
- 已保存记录和剧照离线可用；标记已看时尝试缓存官方海报。
- 设置中导出和恢复完整 ZIP 备份，包含笔记、海报和剧照。恢复会替换当前数据，并需确认。

## 联网搜索

在设置中的 `TMDB API Key` 填写自己的 TMDB v3 API Key。密钥只保存在手机上，不随源码、安装包或备份公开。未配置时可以手动记录电影。搜索和首次海报下载需要联网。

## 安装与构建

安装包在 [Releases](https://github.com/imcoda/Seenema/releases)。个人版包名 `ink.c0d4.movieflix`，可与官方版和之前的 Seenema 安装包并存，支持 Android 7.0 以上。

需要 JDK 21、Android SDK 36.1 和 Build Tools 36.0.0。

```sh
bash gradlew assembleDebug testDebugUnitTest
```

Windows 使用 `gradlew.bat`。构建正式签名包时，在本地 `signing.properties` 配置 `storeFile`、`storePassword`、`keyAlias` 和 `keyPassword`，再运行 `assembleRelease`。签名文件不得提交到 Git；后续更新需要使用同一签名。

## 来源与许可

上游作者 Shalen Mathew，许可为 MIT，原许可保留在 [LICENSE](LICENSE)。这是个人修改版，不是上游官方发行。本仓库早期 Seenema 实现已替换，历史记录可在 Git 中查阅；其历史文件仍按原 GPL 许可分发。
