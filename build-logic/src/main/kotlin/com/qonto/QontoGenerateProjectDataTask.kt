package com.qonto

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.slf4j.LoggerFactory

@CacheableTask
open class QontoGenerateProjectDataTask
@Inject constructor(
    private val logger: Logger,
    objects: ObjectFactory,
    layout: ProjectLayout,
) : DefaultTask() {

    @Input
    val projectGroup: Property<String> = objects.property()

    @Input
    val projectName: Property<String> = objects.property()

    @Input
    val projectVersion: Property<String> = objects.property()

    @Input
    @Option(option = "projectDescription", description = "The project description")
    val projectDescription: Property<String> = objects.property<String>()

    @OutputDirectory
    val outputDir: DirectoryProperty =
        objects
            .directoryProperty()
            .convention(layout.buildDirectory.dir("generated/kotlin/com/qonto"))

    @Internal
    val outputFile: RegularFileProperty =
        objects
            .fileProperty()
            .convention { outputDir.file("Project.kt").get().asFile }

    init {
        group = "qonto"
        description = "Generates the project data"
    }

    @TaskAction
    fun run() {
        logger.quiet("Generating project data...")
        logger.quiet("Project group: ${projectGroup.get()}")
        logger.quiet("Project name: ${projectName.get()}")
        logger.quiet("Project version: ${projectVersion.get()}")
        logger.quiet("Project description: ${projectDescription.get()}")

        outputDir.get().asFile.mkdirs()
        outputFile.get().asFile.apply {
            createNewFile()
            writeText(
                """
                    package com.qonto
                    
                    data object Project {
                        const val group: String = "${projectGroup.get()}"
                        const val name: String = "${projectName.get()}"
                        const val version: String = "${projectVersion.get()}"
                        const val description: String = "${projectDescription.get()}"
                    }
                """.trimIndent(),
            )
        }
    }

    companion object {

        const val NAME: String = "generateProjectData"

        fun register(project: Project, qontoExtension: QontoExtension) {
            val generateProjectData: TaskProvider<QontoGenerateProjectDataTask> =
                project.tasks.register<QontoGenerateProjectDataTask>(
                    name = NAME,
                    LoggerFactory.getLogger("qonto"),
                )

            generateProjectData.configure {
                projectGroup.set(project.provider { "${project.group}" })
                projectName.set(project.provider { project.name })
                projectVersion.set(project.provider { "${project.version}" })
                projectDescription.set(qontoExtension.projectDescription)
            }

            project.tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure {
                dependsOn(generateProjectData)
            }

            project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                project.configure<KotlinProjectExtension> {
                    sourceSets.named("main") {
                        kotlin.srcDirs(generateProjectData)
                    }
                }
            }
        }
    }
}
