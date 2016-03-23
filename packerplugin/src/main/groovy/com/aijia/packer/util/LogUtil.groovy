package com.aijia.packer.util

import org.gradle.api.Project

class LogUtil {

    /**Debug*/
    static void d(Project project, String msg) {
        //project.logger.debug(msg)
        println msg
    }

    /**Warn*/
    static void w(Project project, String msg) {
        //project.logger.warn(msg)
        println msg
    }

    /**Error*/
    static void e(Project project, String msg) {
        //project.logger.warn(msg)
        //throw new MissingPropertyException(msg)
        println msg
    }

    /**Info*/
    static void i(Project project, String msg) {
        //project.logger.info(msg)
        println msg
    }
}