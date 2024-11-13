package com.qonto

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.kotlin.dsl.apply

class QontoPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(BasePlugin::class)
        target.logger.quiet("Hello from QontoPlugin!")
        QontoGenerateProjectDataTask.register(target)
    }
}
