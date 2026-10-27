# MovieFlix

本仓库是 [shalenMathew/MovieFlix_App](https://github.com/shalenMathew/MovieFlix_App) 的定制分支，基于 MovieFlix 2.2，上游提交 `c7ca2763f9dee568f94cbd424983f36cf5ae0ce9`，保留原 MIT 许可与版权。仓库名沿用 Seenema，应用基于 MovieFlix。

保留 MovieFlix 的主题、字体、图标、电影信息、笔记与备份。仅保留 Search / Gallery / Settings，精简推荐、待看、清单、追剧、提醒等无关入口。Gallery 按个人需求调整为海报与剧照并排的时间线。

## 2.2.8

- API 请求改用 `api.tmdb.org` 替代入口，沿用 TMDB 电影编号、数据格式和语言设置。
- 新版本名、Release 标签和 APK 文件名不再包含 `personal`；包名与签名保持兼容，可覆盖安装旧版。

## 2.2-personal.7

- 缩小 Gallery 标题与首条月份之间的间距。
- 每部电影单选一张剧照，添加后不再显示加号；旧版已有多张图片保留。
- 标题旁的按钮可切换横向时间线卡片与原版三列海报网格（含评分），记住上次选择。

## 2.2-personal.6

- 每部电影使用一张圆角横卡，左侧为电影海报，右侧为个人剧照，按观看年月纵向排列。
- 点击海报打开电影信息和笔记；剧照从详情移至时间线，点击后以共享元素动画进入独立大图查看器，支持左右翻页、双指缩放、双击缩放和删除单张图片。
- 点击剧照区的加号导入图片，已有剧照和备份继续保留。
- 观看年月改为与应用配色一致的圆角底部弹层，提供年份切换、直接输入年份和月份网格。

## 2.2-personal.5

- 加入画廊后，点击电影详情中的观看年月，可选择实际观看的年份和月份。
- 保存后时间线自动按观看年月从新到旧排列，可随时再次修改。
- 沿用现有日期字段和备份格式，旧记录、笔记和剧照保留。

## 2.2-personal.4

- 修复深色搜索框输入文字与光标不可读。
- 键盘弹出时隐藏底部导航，收起后恢复固定底栏。
- 移除电影详情的空 More Options 入口。
- 仅保留中文和英文；首次跟随系统，语言页选中项与实际界面一致。

## Gallery

- Gallery 页面只保留一个标题，移除重复标签和页面内搜索栏。
- 海报与剧照按观看月份分组，从新到旧显示；未编辑时沿用原先加入画廊的日期。旧记录缺少日期时归入未注明日期，内容不会丢失。
- 详情按钮与提示统一为 Add to gallery / Remove from gallery。
- Settings → App Language 提供 English 和中文（简体），覆盖界面、月份标题与通知提示；选择后保存语言设置。
- 移除 Settings 页的 Made by 页脚。

现有本地数据库和图片路径保持兼容。

联网搜索使用 [F-Droid 公开的上游构建配置](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.shalenmathew.movieflix.yml)，依赖 TMDB 服务和该公开配置继续可用。可通过 `MOVIE_API_KEY` 环境变量或 Gradle 的 `movieApiKey` 属性覆盖构建配置。

安装包见 [Releases](https://github.com/imcoda/Seenema/releases)。包名 `ink.c0d4.movieflix`，Android 7.0 以上。沿用原个人版签名，可以直接覆盖安装保留记录、笔记和剧照；不需先卸载。

构建需要 JDK 21、Android SDK 36.1、Build Tools 36.0.0：

```sh
bash gradlew assembleDebug testDebugUnitTest
```

Windows 使用 `gradlew.bat`。正式构建在本地 `signing.properties` 设置 `storeFile`、`storePassword`、`keyAlias`、`keyPassword`，运行 `assembleRelease`。签名文件不能上传。

上游作者 Shalen Mathew，MIT 许可保留在 LICENSE 中。早期 Seenema 的历史文件仍按其原 GPL 许可分发。
