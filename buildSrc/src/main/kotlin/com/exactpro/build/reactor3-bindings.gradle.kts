package com.exactpro.build

import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.*

plugins {
    java
}

val vertxVersion: String by rootProject.ext
val reactorVersion: String by rootProject.ext

val vertxSources: Configuration by configurations.creating
val buildDirectory: DirectoryProperty = project.layout.buildDirectory
val vertxSourcesDirectory = buildDirectory.dir("generated/sources/vertx/java")

sourceSets {
    main {
        java {
            srcDir(buildDirectory.dir("generated/sources/annotationProcessor/java/codegen"))
        }
    }
    register("codegen") {
        java {
            srcDir(vertxSourcesDirectory)
        }
    }
}

dependencies {
    "codegenAnnotationProcessor"(project(":reactor3-gen"))

    "codegenImplementation"(platform("io.vertx:vertx-dependencies:$vertxVersion"))
    "codegenImplementation"(platform("io.projectreactor:reactor-bom:$reactorVersion"))
    "codegenImplementation"("io.vertx:vertx-codegen")
    "codegenImplementation"("io.vertx:vertx-docgen")
    "codegenImplementation"("io.vertx:vertx-rx-gen:$vertxVersion")
    "codegenImplementation"("io.vertx:$vertxModuleName") {
        isTransitive = true
        exclude("io.vertx", vertxModuleName)
    }
    if (project.name != "vertx-core-reactor3") {
        "codegenImplementation"(project(":vertx-core-reactor3", "default"))
    }

    implementation(platform("io.vertx:vertx-dependencies:$vertxVersion"))
    implementation(platform("io.projectreactor:reactor-bom:$reactorVersion"))
    implementation("io.vertx:$vertxModuleName")
    if (project.name != "vertx-core-reactor3") {
        implementation(project(":vertx-core-reactor3", "default"))
    }

    testImplementation("io.vertx:vertx-core::tests")
    testImplementation("junit:junit")

    vertxSources("io.vertx:$vertxModuleName:$vertxVersion:sources")
}

tasks {
    val extractVertxSources = register<Copy>("extractVertxSources") {
        from(zipTree(vertxSources.jarFile(vertxModuleName, "sources"))) {
            includeEmptyDirs = false
            include("io/vertx/**/*.java", "examples/**/*.java")
            exclude(
                "**/impl/**/*.java",
                "io/vertx/groovy/**",
                "io/vertx/reactivex/**",
                "io/vertx/rxjava/**",
                "examples/override/**",
                "io/vertx/ext/sql/**",
                "io/vertx/ext/jdbc/**",
                "io/vertx/servicediscovery/types/JDBCDataSource.java",
                "io/vertx/servicediscovery/types/JDBCAuthentication.java",
            )
        }
        into(vertxSourcesDirectory)
    }

    named<JavaCompile>("compileCodegenJava") {
        dependsOn(extractVertxSources)

        options.compilerArgs.addAll(listOf(
            "-processor", "io.vertx.codegen.CodeGenProcessor",
            "-Acodegen.generators=Reactor3",
        ))
    }

    compileJava {
        dependsOn("compileCodegenJava")
    }
}
