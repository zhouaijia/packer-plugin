package com.aijia.packer.tasks

import com.aijia.packer.util.LogUtil
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction


class CopyApk2TargetDir extends Copy {
    String variantName

    CopyApk2TargetDir() {
        setDescription('copy apk to target output dir, and rename it')
    }

    @TaskAction
    void showMessage() {
        LogUtil.i(project,"CopyApk2TargetDir-->${name}-->showMessage-->copy ${variantName} to target output dir,and rename it")
    }
}