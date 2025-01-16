plugins {
    java
}

val vertxVersion by ext("4.5.11")

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
        implementation(platform("io.vertx:vertx-dependencies:$vertxVersion"))
        implementation(platform("io.projectreactor:reactor-bom:2024.0.1"))

        constraints {
            testImplementation("junit:junit:4.13.1")
        }
    }
}
