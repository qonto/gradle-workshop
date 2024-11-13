package com.qonto

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

class QontoPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(BasePlugin::class)
        target.logger.quiet("Hello from QontoPlugin!")

        QontoGenerateProjectDataTask.register(target)

        target.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            target.configure<KotlinProjectExtension> {
                sourceSets.named("main") {
                    kotlin.srcDirs(target.layout.buildDirectory.dir("generated/kotlin"))
                }
            }
        }
    }
}
