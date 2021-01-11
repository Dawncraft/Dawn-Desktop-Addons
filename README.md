### Dawn-Desktop-Addons
### 曙光桌面小部件

An Android app provides a live wallpaper and some app widgets.

一个提供了一个可加载模型的动态桌面壁纸和一些桌面小部件的安卓应用程序。

Maintained by Dawncraft Studio.

由曙光工艺工作室维护

## Features
## 特性

1. A live wallpaper that can render some custom models.(WIP)
   一个可加载自定义模型的动态壁纸(未完成)
2. An app widget that can monitor Novel coronavirus pneumonia's statistics.The API comes from Tencent News.
   一个用于监测新型冠状病毒肺炎的桌面小工具，API来自于腾讯新闻
   ![Screenshot1](/screenshot-1.png)
3. An app widget that can open/close zen mode.
   一个用于开启或关闭勿扰模式的锁屏通知
   ![Screenshot2](/screenshot-2.png)

## Bugs
1. 各种bug

## TODO
1. ~~加入缓存~~
2. 将获取新冠肺炎数据放在Service中(可能不会加入)
3. 新冠肺炎小部件可选择地区
4. 新冠肺炎小部件打开自定义界面, 而非网页
5. ~~支持其他数据源~~(放弃支持)
6. 桌面小部件适配各种屏幕尺寸

## 测试机型
- HM Note 1S(Android4.4.4/MIUI9.2稳定版) 新冠肺炎小部件可用(由old-android分支维护)
- ASUS Z010DA(Android5.0.2) 新冠肺炎小部件可用
- Samsung Galaxy Note4(Android6.0.1) 新冠肺炎小部件可用
- HUAWEI 畅享7 Plus(Android7.0/EMUI5.1.3) 完全可用
- OPPO A7(Android8.1.0/ColorOSV5.2) 完全可用
- Honor 30(Android10/MagicUI4.0.0) 完全可用

另外附上打开设置界面的方法
```bash
adb shell
am start -n io.github.dawncraft.desktopaddons/io.github.dawncraft.desktopaddons.SettingsActivity
```

## 特别致谢
1. 所有在疫情中奉献自己的医护人员
2. 腾讯新闻API

## 更新日志
懒得写
