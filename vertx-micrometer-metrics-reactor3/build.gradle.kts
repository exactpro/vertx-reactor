val micrometerVersion: String by rootProject.ext

dependencies {
    codegenImplementation("io.micrometer:micrometer-registry-jmx:$micrometerVersion")
    codegenImplementation("io.micrometer:micrometer-registry-influx:$micrometerVersion")
    codegenImplementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    compileOnly("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    implementation(project(":vertx-web-reactor3"))
    implementation("io.vertx:vertx-web")
}