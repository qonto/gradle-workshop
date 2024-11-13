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
