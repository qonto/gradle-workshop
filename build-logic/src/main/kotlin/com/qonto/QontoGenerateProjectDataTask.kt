package com.qonto

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.slf4j.LoggerFactory

open class QontoGenerateProjectDataTask
@Inject constructor(
    private val logger: Logger
) : DefaultTask() {

    init {
        group = "qonto"
        description = "Generates the project data"
    }

    @TaskAction
    fun run() {
        logger.quiet("Generating project data...")
    }

    companion object {

        const val NAME: String = "generateProjectData"

        fun register(project: Project) {
            val generateProjectData: TaskProvider<QontoGenerateProjectDataTask> =
                project.tasks.register<QontoGenerateProjectDataTask>(
                    name = NAME,
                    LoggerFactory.getLogger("qonto"),
                )
        }
    }
}
