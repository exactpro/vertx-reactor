import com.exactpro.build.jarFile

val vertxVersion: String by rootProject.ext
val reactorVersion: String by rootProject.ext

val vertxSources: Configuration by configurations.creating
val buildDirectory = project.layout.buildDirectory

sourceSets {
    test {
        java {
            srcDir(buildDirectory.dir("tck/java"))
            srcDir(buildDirectory.dir("vertx-core/java"))
        }
//        resources {
//            srcDir(buildDirectory.dir("tck/resources"))
//        }
    }
}

dependencies {
    implementation(platform("io.vertx:vertx-dependencies:$vertxVersion"))
    implementation(platform("io.projectreactor:reactor-bom:$reactorVersion"))
    implementation("io.projectreactor:reactor-core")
    implementation("io.vertx:vertx-codegen")
    implementation("io.vertx:vertx-core")
    implementation("io.vertx:vertx-docgen")
    implementation("io.vertx:vertx-rx-gen:$vertxVersion")

    vertxSources("io.vertx:vertx-codegen:$vertxVersion:tck-sources")
    vertxSources("io.vertx:vertx-core:$vertxVersion:sources")

    testAnnotationProcessor(project)
    //testAnnotationProcessor(buildDirectory.dir("resources/test").get().asFileTree)
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.vertx:vertx-rx-gen:$vertxVersion:tests")
    testImplementation("io.vertx:vertx-core::tests")
    testImplementation("junit:junit")
}

tasks {
    compileTestJava {
        dependsOn("extractTckSources")
        dependsOn("extractTckResources")
        dependsOn("extractStreamsSources")
//        dependsOn("processTestResources")

        options.compilerArgs.addAll(listOf(
            "-processor", "io.vertx.codegen.CodeGenProcessor",
//    "-Acodegen.output=${project.projectDir}/src/test",
            "-Acodegen.generators=Reactor3",
        ))
    }

//    processTestResources {
//        dependsOn("extractTckResources")
//    }

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

    register<Copy>("extractStreamsSources") {
        from(zipTree(vertxSources.jarFile("vertx-core", "sources"))) {
            includeEmptyDirs = false
            include("io/vertx/core/streams/*.java")
            into("java")
        }
        into(buildDirectory.dir("vertx-core"))
    }
}
