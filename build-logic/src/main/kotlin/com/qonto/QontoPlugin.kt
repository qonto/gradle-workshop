package com.qonto

import org.gradle.api.Plugin
import org.gradle.api.Project

class QontoPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.logger.quiet("Hello from QontoPlugin!")
        QontoGenerateProjectDataTask.register(target)
    }
}
