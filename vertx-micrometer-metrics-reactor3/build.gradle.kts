val micrometerVersion: String by rootProject.ext

dependencies {
    codegenImplementation("io.micrometer:micrometer-registry-jmx:$micrometerVersion")
    codegenImplementation("io.micrometer:micrometer-registry-influx:$micrometerVersion")
    codegenImplementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    codegenImplementation("io.vertx:vertx-web")
    codegenImplementation(project(":vertx-web-reactor3"))

    compileOnly("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    implementation(project(":vertx-web-reactor3", "default"))
}