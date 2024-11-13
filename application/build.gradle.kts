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
