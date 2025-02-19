package com.exactpro.build

import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.*

plugins {
    java
}

val vertxVersion: String by rootProject.ext

val vertxModuleName: String = name.removeSuffix("-reactor3")
val vertxSources: Configuration by configurations.creating
val buildDirectory: DirectoryProperty = project.layout.buildDirectory

sourceSets {
    main {
        java {
            srcDir(buildDirectory.dir("generated/sources/annotationProcessor/java/codegen"))
        }
    }
    register("codegen") {
        java {
            srcDir(buildDirectory.dir("vertx/java"))
        }
    }
}

dependencies {
    "codegenAnnotationProcessor"(project(":reactor3-gen"))

    "codegenImplementation"(project(":reactor3-gen", "default"))
    "codegenImplementation"("io.vertx:$vertxModuleName") {
        isTransitive = true
        exclude("io.vertx", vertxModuleName)
    }

    implementation(project(":reactor3-gen", "default"))
//    implementation("io.projectreactor:reactor-core")
    implementation("io.vertx:$vertxModuleName")

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
                "examples/**",
                "io/vertx/ext/sql/**",
                "io/vertx/ext/jdbc/**",
                "io/vertx/servicediscovery/types/JDBCDataSource.java",
                "io/vertx/servicediscovery/types/JDBCAuthentication.java",
            )
        }
        into(buildDirectory.dir("vertx/java"))
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
