package com.exactpro.build

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.io.File

fun Configuration.jarFile(moduleName: String, classifier: String): File {
    return this.resolvedConfiguration.resolvedArtifacts
        .first { it.name == moduleName && it.classifier == classifier }
        .file
}

val Project.vertxModuleName: String
    get() = this.name.removeSuffix("-reactor3")
