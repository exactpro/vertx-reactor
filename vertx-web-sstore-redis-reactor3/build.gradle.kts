dependencies {
    codegenImplementation(project(":vertx-redis-client-reactor3"))
    codegenImplementation(project(":vertx-web-reactor3"))

    implementation(project(":vertx-redis-client-reactor3"))
    implementation(project(":vertx-web-reactor3"))
}