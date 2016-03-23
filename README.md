# packer-plugin
A gradle plugin for apk packer.

#如果报 Task 'buildApkRelease' not found in root project ，则原因可能有：
#1.没有在相应的module下的build.gradle里面添加对本插件的依赖
#2.'buildApkRelease'根本就没有成功生成，需要调试代码