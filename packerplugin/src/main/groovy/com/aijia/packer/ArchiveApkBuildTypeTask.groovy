package com.aijia.packer

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class ArchiveApkBuildTypeTask extends DefaultTask {
    String typeName

    ArchiveApkBuildTypeTask() {
        setDescription('copy archives of this build type to output dir')
    }

    @TaskAction
    void showMessage() {
        LogUtil.i(project, "ArchiveApkBuildTypeTask-->${name}-->showMessage-->copy archives of this build type to output dir for ${typeName}")
    }
}