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
