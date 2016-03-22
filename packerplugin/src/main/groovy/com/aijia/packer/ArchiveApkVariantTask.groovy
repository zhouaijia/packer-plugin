package com.aijia.packer

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction


class ArchiveApkVariantTask extends Copy {
    String variantName

    ArchiveApkVariantTask() {
        setDescription('copy variant apk to output and rename apk')
    }

    @TaskAction
    void showMessage() {
        LogUtil.i(project,"ArchiveApkVariantTask-->${name}-->showMessage-->copy archives of ${variantName} to output dir")
    }
}