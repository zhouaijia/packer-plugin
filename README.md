# packer-plugin
A gradle plugin for apk packer.

使用方法

在相应module下的 build.gradle文件中添加对本插件的依赖 ：

buildscript {

    repositories {
        mavenCentral()
    }


    dependencies{
        classpath 'com.aijia.packer:packerplugin:1.0.6'
    }
}

apply plugin: 'packer'

packer {

    //指定apk输出路径
    targetOutputDir = file(new File(project.rootProject.buildDir.path, "myApk"))
    //指定manifest中要替换的字符串，不是以"_ID"结尾的字符串都会被替换成渠道名，否则会被替换成渠道Id
    manifestMatcher = ['UMENG_CHANNEL','XXSY_CHANNEL_ID']
    //指定输出的apk的名称前缀。例：如果前缀是xxsy_，且有baidu这个渠道号，则有xxsy_baidu.apk
    apkPrefixName = 'xxsy_'
    apkSuffixName = '_xxsy'
}


多渠道打包

在相应的project根目录下创建一个纯文本文件，内容是渠道名列表。每行一个渠道号，渠道名与渠道Id之间以"-"分割，#后面为注释。列表解析的时候会自动忽略空白行，但是格式不规范会报错。示例：


baidu-200#百度

xiaomi-300#小米

wandoujia-400#豌豆荚

huawei-500#华为


批量打包命令

示例（在project根目录执行）：

gradlew -Pchannel=channels.txt clean buildApkRelease

如果文件在module目录下，比如sample，则应为

gradlew -Pchannel=sample/channels.txt clean buildApkRelease


问题

如果报 Task 'buildApkRelease' not found in root project ，则原因可能有：

1.没有在相应的module下的build.gradle里面添加对本插件的依赖
