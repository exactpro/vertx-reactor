dependencies {
    codegenImplementation(project(":vertx-sql-client-reactor3"))
    implementation(project(":vertx-sql-client-reactor3"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}
