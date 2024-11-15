# Gradle Workshop

The workshop will guide you through the process of creating Gradle tasks, focusing on inputs,
outputs, and making tasks cacheable.

You'll also learn:

- How to create a Gradle project extension to extend plugin functionality.
- How to create a Gradle task that codegen a Kotlin file.
- Wire the generated code with the Kotlin source sets.
- If time, how to use the `Problems` API to report issues.

Our hands-on project involves building a Gradle plugin that generates a Kotlin file containing
project metadata such as project version, group and name. This practical experience will cover
creating Gradle plugins, tasks, and extensions, as well as reporting issues and integrating with
source sets.

## Start

The repository contains everything needed to start the workshop.

- The `build-logic` included build contains the Gradle plugin which is going to be created.
- The `application` module will be used as a simple application to run the generated code by the
  plugin.

To run the application, use the next CLI command:

```shell
./gradlew run
```

## Step 1: Create the Qonto plugin ✅

<details>
<summary>Create the Gradle plugin by extending the `Plugin` interface.</summary>

- Right-click on the `build-logic` module.
- Create the directory `src/main/kotlin/com/qonto/`.
- Create the file `QontoPlugin.kt` in the directory.
- Create the class `QontoPlugin` and extends the `Plugin` interface using `Project` as its type
  parameter.

```kotlin
package com.qonto

import org.gradle.api.Plugin
import org.gradle.api.Project

class QontoPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.logger.quiet("Hello from QontoPlugin!")
    }
}
`````

</details>

<details>
<summary>Register the plugin in the `build-logic` module with the `qonto` id.</summary>

- Open the `build.gradle.kts` file in `build-logic` module.
- Add the following code to the file below the plugins block.

```kotlin
plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("QontoPlugin") {
            id = "qonto"
            implementationClass = "com.qonto.QontoPlugin"
        }
    }
}
```

</details>

<details>
<summary>Add it to the version catalog.</summary>

- Open the `libs.versions.toml` file inside the `gradle` directory.
- Add the plugin to the bottom of the `plugins` section and sync the Gradle project.

```toml
[versions]
kotlin = "2.0.21"

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
qonto = { id = "qonto" } # Add this line
```

</details>

<details>
<summary>Apply the plugin in the `application` project.</summary>

- Open the `build.gradle.kts` file inside the `application` project.
- Apply the plugin in the `plugins` block.

```kotlin
plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.qonto) // Add this line
}

application {
    mainClass = "com.qonto.application.MainKt"
}

group = "com.qonto"
version = "1.0.0"
```

</details>

## Step 2: Create the QontoGenerateProjectDataTask task ✅

<details>
<summary>Create a task with the minimum amount of code.</summary>

- Create the file `QontoGenerateProjectDataTask.kt` in the `com.qonto` package.
- Create the class `QontoGenerateProjectDataTask` class and extends the `DefaultTask` class.

```kotlin
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
```

</details>

<details>
<summary>Register the task.</summary>

- Call the `register` method on the task `companion object` within the `apply` block in the plugin.

```kotlin
package com.qonto

import org.gradle.api.Plugin
import org.gradle.api.Project

class QontoPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.logger.quiet("Hello from QontoPlugin!")
        QontoGenerateProjectDataTask.register(target) // Add this line
    }
}
```

</details>

<details>
<summary>Apply the base plugin.</summary>

- Use the `pluginManager` to apply the `BasePlugin` plugin

```kotlin
package com.qonto

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin // Add this line
import org.gradle.kotlin.dsl.apply // Add this line

class QontoPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(BasePlugin::class) // Add this line
        target.logger.quiet("Hello from QontoPlugin!")
        QontoGenerateProjectDataTask.register(target)
    }
}
```

</details>

<details>
<summary>Wire the task with the `assemble` task.</summary>

- Use the `named` method on the `tasks` to get the `assemble` task.
- Use `dependsOn` to make the `assemble` task depend on the `generateProjectData` task.

```kotlin
package com.qonto

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.BasePlugin // Add this line
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
            // Add these lines
            project.tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure {
                dependsOn(generateProjectData)
            }
        }
    }
}
```

</details>

## Step 3: Add inputs and outputs to the task ✅

<details>
<summary>Make the task cacheable.</summary>

- Add the `@CacheableTask` annotation to the `QontoGenerateProjectDataTask` class.

```kotlin
package com.qonto

// ...
import org.gradle.api.tasks.CacheableTask // Add this line

// ...

@CacheableTask // Add this line
open class QontoGenerateProjectDataTask
@Inject constructor(
    private val logger: Logger
) : DefaultTask() {
    // ...
}
```

</details>

<details>
<summary>Add inputs to the task and configure them.</summary>

- Use the `@Input` annotation to mark the properties as inputs in the
  `QontoGenerateProjectDataTask`.
- Wire them within the `configure` method block from the `TaskProvider`.
- Use the `provider` lambda to do lazy evaluation of the provided properties.

```kotlin
package com.qonto

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
import org.slf4j.LoggerFactory

@CacheableTask
open class QontoGenerateProjectDataTask
@Inject constructor(
    private val logger: Logger,
    private val objects: ObjectFactory,
) : DefaultTask() {

    @Input
    val projectGroup: Property<String> = objects.property()

    @Input
    val projectName: Property<String> = objects.property()

    @Input
    val projectVersion: Property<String> = objects.property()

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
    }

    companion object {

        const val NAME: String = "generateProjectData"

        fun register(project: Project) {
            val generateProjectData: TaskProvider<QontoGenerateProjectDataTask> =
                project.tasks.register<QontoGenerateProjectDataTask>(
                    name = NAME,
                    LoggerFactory.getLogger("qonto"),
                )

            generateProjectData.configure {
                projectGroup.set(project.provider { "${project.group}" })
                projectName.set(project.provider { project.name })
                projectVersion.set(project.provider { "${project.version}" })
            }

            project.tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure {
                dependsOn(generateProjectData)
            }
        }
    }
}
```

</details>

<details>
<summary>Add outputs to the task and configure them.</summary>

- Use the `@OutputDirectory` annotation to mark the `outputDir` property as an output in the
  `QontoGenerateProjectDataTask`.
- Use the `@Internal` annotation to mark the `outputFile` property as an internal property in the
  `QontoGenerateProjectDataTask`.

```kotlin
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
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
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
    }

    companion object {

        const val NAME: String = "generateProjectData"

        fun register(project: Project) {
            val generateProjectData: TaskProvider<QontoGenerateProjectDataTask> =
                project.tasks.register<QontoGenerateProjectDataTask>(
                    name = NAME,
                    LoggerFactory.getLogger("qonto"),
                )

            generateProjectData.configure {
                projectGroup.set(project.provider { "${project.group}" })
                projectName.set(project.provider { project.name })
                projectVersion.set(project.provider { "${project.version}" })
            }

            project.tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure {
                dependsOn(generateProjectData)
            }
        }
    }
}
```

</details>

## Step 4: Change the task implementation to codegen a file and wire it with the Kotlin source set ✅

<details>
<summary>Change the task implementation to generate a file by using the inputs and outputs.</summary>

- Use the `outputFile` and `outputDir` properties to generate a file with the project data.

```kotlin
package com.qonto

// ...

@CacheableTask
open class QontoGenerateProjectDataTask
@Inject constructor(
    private val logger: Logger,
    objects: ObjectFactory,
    layout: ProjectLayout,
) : DefaultTask() {

    // ...

    @TaskAction
    fun run() {
        // ...

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
                    }
                """.trimIndent(),
            )
        }
    }
    // ...
}
```

</details>

<details>
<summary>Add the generated directory to the main Kotlin source set (WRONG WAY).</summary>

- Use `pluginManager` to react to the `org.jetbrains.kotlin.jvm` plugin being applied.
- Use the `configure` method on the `KotlinProjectExtension` to add the generated directory to the
  main Kotlin source set.
- Run `./gradlew assemble` or `./gradlew run` to see the issue.

```kotlin
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
```

</details>

<details>
<summary>Fix the issue above by wiring the task directly with the Kotlin source set.</summary>

- Use the `named` method on the `sourceSets` to get the `main` source set.
- Use the `kotlin.srcDirs` method to add the task outputs to the source set.
- Run `./gradlew assemble` or `./gradlew run` to see the task being executed.
- Modify the `main` function to print the generated project data.

```kotlin
package com.qonto

// ...

@CacheableTask
open class QontoGenerateProjectDataTask
@Inject constructor(
    private val logger: Logger,
    objects: ObjectFactory,
    layout: ProjectLayout,
) : DefaultTask() {
    // ...

    companion object {

        const val NAME: String = "generateProjectData"

        fun register(project: Project) {
            // ..

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
```

```kotlin
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
```

```kotlin
package com.qonto.application

fun main() {
    println(
        """
            Project data:
            Group: ${com.qonto.Project.group}
            Name: ${com.qonto.Project.name}
            Version: ${com.qonto.Project.version}
        """.trimIndent()
    )
}

```

</details>

## Step 5: Create the QontoExtension to allow the user to specify default values [IN PROGRESS]

<details>
<summary>Create the QontoExtension.</summary>

- Create the file `QontoExtension.kt` in the `com.qonto` package.
- Create the class `QontoExtension` and add the `projectDescription` property.

```kotlin
package com.qonto

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.property

open class QontoExtension
@Inject constructor(
    objects: ObjectFactory,
) {

    val projectDescription: Property<String> =
        objects.property<String>().convention("Gradle workshop")

    companion object {

        const val NAME = "qonto"

        fun register(project: Project): QontoExtension = project.extensions.create(NAME)
    }
}
```

</details>

<details>
<summary>Change the task implementation and wire its configuration with the extension.</summary>

- Add the `projectDescription` property as input in the `QontoGenerateProjectDataTask`.
- Use the `qontoExtension` to wire the `projectDescription` property of the task in the
  `PluginQonto`.
- Modify the `build.gradle.kts` file in the `application` module to use the `qonto` extension.
- Modify the `main` function to print the generated project data with the `projectDescription`.
- Run `./gradlew run` to see the task being executed.

```kotlin
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
```

```kotlin
package com.qonto

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.kotlin.dsl.apply

class QontoPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val qontoExtension: QontoExtension = QontoExtension.register(target)
        target.pluginManager.apply(BasePlugin::class)
        target.logger.quiet("Hello from QontoPlugin!")

        QontoGenerateProjectDataTask.register(target, qontoExtension)
    }
}
```

```kotlin
plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.qonto)
}

application {
    mainClass = "com.qonto.application.MainKt"
}

group = "com.qonto"
version = "1.0.0"

qonto {
    projectDescription = "The Qonto Gradle Workshop!"
    // projectDescription.set("Qonto Workshop!") same as above due to the new Kotlin Compiler plugin
}
```

```kotlin
package com.qonto.application

fun main() {
    println(
        """
            Project data:
            Group: ${com.qonto.Project.group}
            Name: ${com.qonto.Project.name}
            Version: ${com.qonto.Project.version}
            Additional lines: ${com.qonto.Project.description}
        """.trimIndent()
    )
}
```

</details>

## Step 6: Change one task's input to be an option

<details>
<summary>Change the task's input to be an option.</summary>

- Add the `@Option` annotation to the `projectDescription` property in the
  `QontoGenerateProjectDataTask`.

```kotlin
package com.qonto

// ...
import org.gradle.api.tasks.options.Option

// ...

@CacheableTask
open class QontoGenerateProjectDataTask
@Inject constructor(
    private val logger: Logger,
    objects: ObjectFactory,
    layout: ProjectLayout,
) : DefaultTask() {

    // ...

    @Input
    @Option(option = "projectDescription", description = "The project description")
    val projectDescription: Property<String> = objects.property<String>()

    // ...
}

```

</details>

<details>
<summary>Run the task via CLI by passing the option with a different value.</summary>

- Run the task with the `--projectDescription` option to see the new value.

```shell
./gradlew run generateProjectData --projectDescription="New project description!"
```

- Check the output to see the new project description.

</details>
