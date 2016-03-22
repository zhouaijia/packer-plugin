package com.aijia.packer

import org.gradle.api.Project

class PackerPluginParams {
    /**apk prefix name*/
    String apkPrefixName

    /**archive task output dir*/
    File archiveOutput

    /**manifest meta-data key list*/
    List<String> manifestMatcher

    PackerPluginParams(Project project) {
        archiveOutput = new File(project.rootProject.buildDir, "archives")
    }
}