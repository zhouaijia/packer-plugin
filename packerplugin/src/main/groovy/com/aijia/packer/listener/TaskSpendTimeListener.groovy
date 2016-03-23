package com.aijia.packer.listener

import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.BuildListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.util.Clock

/**
 * Analyse every task spends time
 * */
public class TaskSpendTimeListener implements TaskExecutionListener, BuildListener{
    private Clock clock
    private times = []

    @Override
    void beforeExecute(Task task) {//某个任务执行前
        clock = new Clock()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {//某个任务执行后
        def ms = clock.timeInMs
        times.add([ms,task.path])
        //task.project.logger.warn "${task.path} spend ${ms} ms"
    }

    @Override
    void buildStarted(Gradle gradle) {

    }

    @Override
    void settingsEvaluated(Settings settings) {

    }

    @Override
    void projectsLoaded(Gradle gradle) {

    }

    @Override
    void projectsEvaluated(Gradle gradle) {

    }

    @Override
    void buildFinished(BuildResult buildResult) {
        println "Every task spend time:"
        for( time in times) {
            if(time[0] >= 50) {
                //%7s 打印数字，%s 打印字符串
                printf "%7s ms %s\n", time
            }
        }
    }
}