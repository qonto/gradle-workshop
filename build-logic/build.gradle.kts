plugins {
    `kotlin-dsl`
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
