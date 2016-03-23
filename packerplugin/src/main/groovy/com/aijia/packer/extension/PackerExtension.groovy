package com.aijia.packer.extension

import org.gradle.api.Project

class PackerExtension {
    /**apk prefix name*/
    String apkPrefixName

    /**apk suffix name*/
    String apkSuffixName

    /**target output dir
     * 这里之所以传File而不传String类型的参数，是为了防止在遍历渠道时循环创建File实例
     * */
    File targetOutputDir

    /**manifest meta-data key list*/
    List<String> manifestMatcher

    PackerExtension(Project project) {
        apkPrefixName = ""
        apkSuffixName = ""
        targetOutputDir = new File(project.rootProject.buildDir, "myApk")
    }
}