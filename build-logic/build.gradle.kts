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

dependencies {
    implementation(libs.plugins.kotlin.jvm.artifact)
}

val Provider<PluginDependency>.artifact: Provider<ExternalModuleDependency>
    get() = map {
        dependencies.create(
            group = it.pluginId,
            name = "${it.pluginId}.gradle.plugin",
            version = it.version.displayName,
        )
    }
