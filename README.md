# MovieFlix 个人版

基于 [MovieFlix 2.2](https://github.com/shalenMathew/MovieFlix_App)，上游提交 `c7ca2763f9dee568f94cbd424983f36cf5ae0ce9`。仓库名沿用 Seenema，应用基于 MovieFlix。

保留 MovieFlix 原版主题、字体、图标、海报卡片、详情页、笔记、剧照相册与备份界面。仅保留 Search / Gallery / Settings，精简推荐、待看、清单、追剧、提醒等无关入口。

## 2.2-personal.4

- 修复深色搜索框输入文字与光标不可读。
- 键盘弹出时隐藏底部导航，收起后恢复固定底栏。
- 移除电影详情的空 More Options 入口。
- 仅保留中文和英文；首次跟随系统，语言页选中项与实际界面一致。

## Gallery

- Gallery 页面只保留一个标题，移除重复标签和页面内搜索栏。
- 海报按加入画廊的月份分组，从新到旧显示；卡片仍用原版样式。旧记录缺少日期时归入未注明日期，内容不会丢失。
- 详情按钮与提示统一为 Add to gallery / Remove from gallery。
- Settings → App Language 提供 English 和中文（简体）。中文包含 237 条可翻译文案、月份标题与通知提示；选择后保存语言设置。
- 移除 Settings 页的 Made by 页脚。

不增加手动录入、日期编辑或时间线切换按钮。现有本地数据库和图片路径保持兼容。

联网搜索使用 [F-Droid 公开的上游构建配置](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.shalenmathew.movieflix.yml)，依赖 TMDB 服务和该公开配置继续可用。可通过 `MOVIE_API_KEY` 环境变量或 Gradle 的 `movieApiKey` 属性覆盖构建配置。

安装包见 [Releases](https://github.com/imcoda/Seenema/releases)。包名 `ink.c0d4.movieflix`，Android 7.0 以上。沿用原个人版签名，可以直接覆盖安装保留记录、笔记和剧照；不需先卸载。

构建需要 JDK 21、Android SDK 36.1、Build Tools 36.0.0：

```sh
bash gradlew assembleDebug testDebugUnitTest
```

Windows 使用 `gradlew.bat`。正式构建在本地 `signing.properties` 设置 `storeFile`、`storePassword`、`keyAlias`、`keyPassword`，运行 `assembleRelease`。签名文件不能上传。

上游作者 Shalen Mathew，MIT 许可保留在 LICENSE 中。早期 Seenema 的历史文件仍按其原 GPL 许可分发。
