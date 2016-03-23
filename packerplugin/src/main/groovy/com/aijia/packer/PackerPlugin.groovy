package com.aijia.packer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.tasks.StopExecutionException

/**
 * 启动插件时，先进行task解析，建立task有向图。解析task时，执行所有非task代码。
 * 解析完task后，就按照之前建立起的task有向图按顺序依次执行各个task。
 * 代码中可能有些task的顺序与gradle系统里面的执行顺序不一样，但是执行插件时顺序却是正确的，
 * 这是因为不少task的执行顺序已经在gradle系统里面被关联好了
 * */
public class PackerPlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = "packer"
    static final String CHANNEL = "channel"

    private Project project
    private PackerPluginParams packerPluginParams

    void apply(Project project) {
        if(!hasAndroidPlugin(project)) {
            throw new ProjectConfigurationException("the android plugin must be applied",null)
        }

        this.project = project
        //监听每个任务的执行耗时
        project.gradle.addListener(new TimeListener())

        applyExtension()
        def hasChannels = applyChannels()
        applyPluginTasks(hasChannels)
    }

    /**应用扩展属性*/
    void applyExtension() {
        project.configurations.create(EXTENSION_NAME).extendsFrom(project.configurations.compile)
        this.packerPluginParams = project.extensions.create(EXTENSION_NAME,PackerPluginParams,project)
    }

    /**应用本组件。参数类型可变*/
    void applyPluginTasks(hasChannels) {
        println '-----------applyPluginTasks------------'
        //解析完所有任务，建立起任务有向图，这样才能找到相应的任务
        project.afterEvaluate {
            def buildTypes = project.android.buildTypes
            LogUtil.d(project,"applyPluginTasks-->build types: ${buildTypes.collect{ it.name }}")
            checkProperties()
            //applySigningConfigs()
            project.android.applicationVariants.all{ variant ->
                println '-------------checkSigningConfig----------'
                checkSigningConfig(variant)
                if(variant.buildType.name != "debug") {
                    if(hasChannels) {
                        LogUtil.d(project,"applyPluginTasks-->markets found,add manifest and archive task.")
                        checkArchiveTask(variant)
                        checkManifest(variant)
                    } else {
                        LogUtil.d(project,"applyPluginTask-->markets not found,check version name.")
                    }
                }
            }
        }
    }

    void checkProperties() {
        LogUtil.d(project,"checkProperties-->manifest:" + packerPluginParams.manifestMatcher)
    }

    /**检查各个版本（release、bug等）的签名配置*/
    void checkSigningConfig(variant) {
        if(variant.buildType.signingConfig == null) {
            LogUtil.d(project,"checkSigningConfig-->signingConfig for ${variant.buildType.name} is null.")
        }
    }

    /**
     * 执行processManifestTask，完成读取AndroidManifest.xml文件，
     * 然后执行自定义的processMetaTask，替换指定字段，
     * 接着再执行processResourcesTask
     * */
    void checkManifest(variant) {
        //如果没有productFlavors，则返回
        if(!variant.productFlavors) {
            LogUtil.w(project,"checkManifest-->${variant.name}: check manifest, no flavors found, ignore.")
            return
        }
        if(!variant.outputs) {
            LogUtil.w(project,"checkManifest-->${variant.name}: check manifest,no outputs found, ignore.")
            return
        }
        if(!packerPluginParams.manifestMatcher) {
            LogUtil.e(project,"checkManifest-->${variant.name}: check manifest, no manifest matcher found, quit.")
            return
        }
        def Task processManifestTask = variant.outputs[0].processManifest
        def Task processResourcesTask = variant.outputs[0].processResources
        def processMetaTask = project.task("modify${variant.name.capitalize()}MetaData",
                type: ModifyManifestTask) {
            manifestFile = processManifestTask.manifestOutputFile
            manifestMatcher = packerPluginParams.manifestMatcher
            flavorName = variant.productFlavors[0].name
            dependsOn processManifestTask
        }
        processResourcesTask.dependsOn processMetaTask
    }

    void checkArchiveTask(variant) {
        if(variant.buildType.signingConfig == null) {
            LogUtil.w(project,"checkArchiveTask-->${variant.name}: signingConfig is null, ignore archive task.")
            return
        }
        if(!variant.buildType.zipAlignEnabled) {
            LogUtil.w(project, "checkArchiveTask-->${variant.name}: zipAlignEnabled = false, ignore archive task.")
            return
        }

        println "checkArchiveTask11111111-->for ${variant.name}"
        LogUtil.d(project,"checkArchiveTask-->for ${variant.name}")

        def String apkName = createApkName(variant)
        def File inputFile = variant.outputs[0].outputFile
        def File outputDir = packerPluginParams.archiveOutput
        LogUtil.d(project,"checkArchiveTask-->intput file: ${inputFile}")
        LogUtil.d(project,"checkArchiveTask-->output dir: ${outputDir}")
        //这里会生成所有渠道的打包任务，并将这些打包任务加入到任务有向图中
        def archiveTask = project.task("archiveApk${variant.name.capitalize()}",
              type: ArchiveApkVariantTask) {
            variantName = variant.name
            from inputFile.absolutePath
            into outputDir.absolutePath
            rename { filename ->
                filename.replace inputFile.name,apkName
            }
            dependsOn variant.assemble
        }

        LogUtil.d(project,"checkArchiveTask-->new task:${archiveTask.name}")
        def buildTypeName = variant.buildType.name
        println "checkArchiveTask222222222-->${buildTypeName}---${variant.name}"
        //若非多渠道打包，则默认只有release一个版本，
        // 此时"release" == variant.name == variant.buildType.name。
        // 否则会出现诸如xiaomiRelease等版本，
        // 此时(variant.name == "xiaomiRelease") ！= (variant.buildType.name == "release")
        if(variant.name != buildTypeName) {
            def Task task = checkArchiveAllTask(buildTypeName)
            //此task将排在各个渠道task的后面
            task.dependsOn archiveTask
        }
    }

    /**
     * archiveApkRelease 本插件的命令入口
     * 此task依赖各个渠道的编译打包task，所以先执行各个渠道的task
     * */
    Task checkArchiveAllTask(buildTypeName) {
        def taskName = "archiveApk${buildTypeName.capitalize()}"
        def task = project.tasks.findByName(taskName)
        LogUtil.d(project,"checkArchiveAllTask-->taskName-->"+taskName)
        //这里可以保证此任务在当前执行中是唯一的
        if(task == null) {
            LogUtil.d(project,"checkArchiveAllTask-->task is null")
            task = project.task(taskName, type: ArchiveApkBuildTypeTask) {
                typeName = buildTypeName
            }
        }
        return task
    }

    String createApkName(variant) {
        def String apkName = "${packerPluginParams.apkPrefixName}${variant.flavorName}.apk"
        def temp = variant.flavorName.split('-')
        if(temp && temp[0] && temp[0].trim()) {
            apkName = "${packerPluginParams.apkPrefixName}${temp[0].trim()}.apk"
        }

        return apkName
    }

    /**解析channels文件
     * 文件由命令行
     *      gradlew -Pchannel=app/channels.txt clean archiveApkRelease
     * 得到
     * */
    boolean applyChannels() {
        if(!project.hasProperty(CHANNEL)) {
            LogUtil.d(project,"applyChannels-->channels property not found,ignore")
            return  false;
        }

        def channelFilePath = project.property(CHANNEL).toString()
        if(!channelFilePath) {
            LogUtil.d(project,"applyChannels-->invalid channel file path,ignore")
            throw new StopExecutionException("invalid channel file path: '${channelFilePath}'")
        }

        File channel = project./*rootProject.*/file(channelFilePath)
        if(!channel.exists()) {
            LogUtil.d(project,"applyChannel-->channel file not found, ignore")
            throw new StopExecutionException("channel file not found: '${channel.absolutePath}'")
        }

        if(!channel.isFile()) {
            LogUtil.d(project,"applyChannel-->channel is not a file,ignore")
            throw new StopExecutionException("channel is not a file: '${channel.absolutePath}'")
        }

        if(!channel.canRead()) {
            LogUtil.d(project,"applyChannel-->channel is not readable,ignore")
            throw new StopExecutionException("channel is not readable: '${channel.absolutePath}'")
        }

        LogUtil.d(project,"applyChannel-->file: ${channelFilePath}")
        def flavors = new HashSet<String>()
        flavors.addAll(project.android.productFlavors.collect { it.name})
        LogUtil.d(project,"applyChannel-->default flavors:" + flavors)

        channel.eachLine { line, number ->
            LogUtil.d(project,"applyChannel-->${number}: '${line}'")
            def parts = line.split('#')
            if(parts && parts[0]) {
                def c = parts[0].trim()
                if(c && !flavors.contains(c)) {
                    LogUtil.d(project,"apply new channel: " + c)
                    project.android.productFlavors.create(c, {})
                }
            } else {
                LogUtil.w(project,"invalid line found in channel file-->${number}:'${line}'")
                //throw new IllegalArgumentException("invalid channel: ${line} at line:${number} in your channel file")
            }
        }

        return true
    }

    /**文件判断项目是否用了android插件*/
    static boolean hasAndroidPlugin(Project project) {
        return project.plugins.hasPlugin("com.android.application")
    }
}