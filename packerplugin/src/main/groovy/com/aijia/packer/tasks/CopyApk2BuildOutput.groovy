package com.aijia.packer.tasks

import com.aijia.packer.util.LogUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class CopyApk2BuildOutput extends DefaultTask {
    String typeName

    CopyApk2BuildOutput() {
        setDescription('copy apk of this build type to output dir')
    }

    @TaskAction
    void showMessage() {
        LogUtil.i(project, "CopyApk2BuildOutput-->${name}-->showMessage-->${description} for ${typeName}")
    }
}