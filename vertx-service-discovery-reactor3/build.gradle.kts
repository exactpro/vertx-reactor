dependencies {
    codegenImplementation("io.vertx:vertx-mongo-client")
    codegenImplementation("io.vertx:vertx-redis-client")
    codegenImplementation("io.vertx:vertx-web")
    codegenImplementation("io.vertx:vertx-web-client")
    codegenImplementation(project(":vertx-mongo-client-reactor3"))
    codegenImplementation(project(":vertx-redis-client-reactor3"))
    codegenImplementation(project(":vertx-web-client-reactor3"))

    implementation(project(":vertx-mongo-client-reactor3", "default"))
    implementation(project(":vertx-redis-client-reactor3", "default"))
    implementation(project(":vertx-web-client-reactor3", "default"))
}