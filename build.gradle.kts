import com.exactpro.build.vertxModuleName

plugins {
    java
}

val vertxVersion by ext("4.5.11")
val reactorVersion by ext("2024.0.1")

val vertxModules: Set<String> =
    project.file("vertx.modules").useLines { lines -> lines.filter { it.isNotBlank() }.toSet() }

project.subprojects {
    apply(plugin = "java-library")

    group = "com.exactpro.vertx"
    version = "$vertxVersion-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release = 8
    }

    dependencies {
        constraints {
            testImplementation("junit:junit:4.13.1")
        }
    }

    if (vertxModuleName in vertxModules) {
        apply(plugin = "com.exactpro.build.reactor3-bindings")
    }
}
