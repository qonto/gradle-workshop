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
