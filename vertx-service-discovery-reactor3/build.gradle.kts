dependencies {
    codegenImplementation("io.vertx:vertx-web")

    // TODO: compileOnly?
    implementation(project(":vertx-mongo-client-reactor3"))
    implementation(project(":vertx-redis-client-reactor3"))
    implementation(project(":vertx-web-client-reactor3"))
    implementation("io.vertx:vertx-mongo-client")
    implementation("io.vertx:vertx-redis-client")
    implementation("io.vertx:vertx-web-client")
}