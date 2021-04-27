package com.strod.moduleinit.plugin.utils

import org.gradle.api.Project

/**
 * Format log
 *
 */
class Logger {
    static org.gradle.api.logging.Logger logger

    static void make(Project project) {
        logger = project.getLogger()
    }

    static void i(String info) {
        if (null != info && null != logger) {
            logger.info("ModuleInit::Register >>> " + info)
        }
    }

    static void e(String error) {
        if (null != error && null != logger) {
            logger.error("ModuleInit::Register >>> " + error)
        }
    }

    static void w(String warning) {
        if (null != warning && null != logger) {
            logger.warn("ModuleInit::Register >>> " + warning)
        }
    }
}
