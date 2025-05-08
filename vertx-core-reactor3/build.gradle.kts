import com.exactpro.build.jarFile

plugins {
    id("com.exactpro.build.reactor3-bindings")
}

val vertxVersion: String by rootProject.ext
val vertxSources: Configuration by configurations.getting
val buildDirectory = project.layout.buildDirectory

sourceSets {
    codegen {
        java {
            srcDir(project.layout.projectDirectory.dir("src/main/java"))
        }
    }
    test {
        java {
            srcDir(buildDirectory.dir("tck/java"))
        }
        resources {
            srcDir(buildDirectory.dir("tck/resources"))
        }
    }
}

dependencies {
    codegenImplementation("com.fasterxml.jackson.core:jackson-databind")
    codegenImplementation("org.apache.logging.log4j:log4j-core")
    codegenImplementation("org.slf4j:slf4j-api")

    implementation("io.projectreactor:reactor-core")
    implementation("io.vertx:vertx-docgen")
    implementation("io.vertx:vertx-rx-gen:$vertxVersion")

    testAnnotationProcessor(project(":reactor3-gen"))
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.vertx:vertx-rx-gen:$vertxVersion:tests")

    vertxSources("io.vertx:vertx-codegen:$vertxVersion:tck-sources")
}

tasks {
    compileTestJava {
        dependsOn("extractTckSources")
        dependsOn("extractTckResources")
//        dependsOn("processTestResources")

        options.compilerArgs.addAll(
            listOf(
                "-processor", "io.vertx.codegen.CodeGenProcessor",
//                "-Acodegen.output=${project.projectDir}/src/test",
                "-Acodegen.generators=Reactor3",
            )
        )
    }

    register<Copy>("extractTckSources") {
        from(zipTree(vertxSources.jarFile("vertx-codegen", "tck-sources"))) {
            includeEmptyDirs = false
            include("**/*.java")
        }
        into(buildDirectory.dir("tck/java"))
    }

    register<Copy>("extractTckResources") {
        from(zipTree(vertxSources.jarFile("vertx-codegen", "tck-sources"))) {
            includeEmptyDirs = false
            include("META-INF/vertx/json-mappers.properties")
        }
//      into(buildDirectory.dir("tck/resources"))
        into(buildDirectory.dir("classes/java/test"))
    }
}
