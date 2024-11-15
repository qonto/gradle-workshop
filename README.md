# Gradle Workshop

## Start

The repository contains everything needed to start the workshop.

- The `build-logic` included build contains the Gradle plugin which is going to be created.
- The `application` module will be used as a simple application to run the generated code by the
  plugin.

## Step 1: Create Qonto plugin

#### Goals

<details>
<summary>Create the Gradle plugin by extending the `Plugin` interface.</summary>

- Right-click on the `build-logic` module.
- Create the directory `src/main/kotlin/com/qonto/`.
- Create the file `QontoPlugin.kt` in the directory.
- Create the class `QontoPlugin` and extends the `Plugin` interface using `Project` as its type parameter.

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
