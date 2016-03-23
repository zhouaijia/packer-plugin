package com.aijia.packer

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


class CleanTask extends DefaultTask {
    @Input
    File target

    CleanTask() {
        setDescription('clean all apk archives in output dir')
    }

    @TaskAction
    void showMessage() {
        LogUtil.i(project,"${name}: ${description}")
    }

    @TaskAction
    void deleteAll() {
        LogUtil.i(project,"${name}: delete all files in $target.absolutePath")
        deleteDir(target)
    }

    static void deleteDir(File file) {
        if(file && file.listFiles()) {
            file.listFiles().sort().each { File f ->
                if(f.isFile()) {
                    f.delete()
                } else {
                    f.deleteDir()
                }
            }
        }
    }
}