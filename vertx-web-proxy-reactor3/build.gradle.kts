dependencies {
    codegenImplementation(project(":vertx-http-proxy-reactor3"))
    codegenImplementation(project(":vertx-web-reactor3"))

    implementation(project(":vertx-http-proxy-reactor3"))
    implementation(project(":vertx-web-reactor3"))
}