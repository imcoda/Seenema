# MovieFlix 个人精简版

基于 [MovieFlix 2.2](https://github.com/shalenMathew/MovieFlix_App)，上游提交 `c7ca2763f9dee568f94cbd424983f36cf5ae0ce9`。仓库名沿用 Seenema，应用基于 MovieFlix。

保留上游原始布局、主题、字体、图标、文案、海报网格、详情页、个人笔记、剧照相册与备份界面。仅精简首页推荐、待看、自建清单、追剧、提醒及无关设置入口。

`2.2-personal.2` 撤回先前新增的时间线切换、手动添加电影、观看日期编辑、密钥设置及零散中文改名。底部使用原版 Search / Library / Settings；电影从原版 Search 搜索并加入 Favourites。

联网搜索使用 [F-Droid 公开的上游构建配置](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.shalenmathew.movieflix.yml)，无需额外设置。它依赖 TMDB 服务及该公开配置继续可用。可通过 `MOVIE_API_KEY` 环境变量或 Gradle 的 `movieApiKey` 属性覆盖构建配置。

安装包见 [Releases](https://github.com/imcoda/Seenema/releases)。包名 `ink.c0d4.movieflix`，Android 7.0 以上；与第一版使用相同签名，可直接覆盖安装，保留本地数据。

构建需要 JDK 21、Android SDK 36.1、Build Tools 36.0.0：

```sh
bash gradlew assembleDebug testDebugUnitTest
```

Windows 使用 `gradlew.bat`。正式构建在本地 `signing.properties` 设置 `storeFile`、`storePassword`、`keyAlias`、`keyPassword`，运行 `assembleRelease`。签名文件不能上传。

上游作者 Shalen Mathew，MIT 许可。早期 Seenema 的历史文件仍按其原 GPL 许可分发。
