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
