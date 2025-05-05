dependencies {
    codegenImplementation(project(":vertx-openapi-reactor3", "default"))
    codegenImplementation(project(":vertx-web-reactor3"))

    implementation(project(":vertx-openapi-reactor3", "default"))
    implementation(project(":vertx-web-reactor3"))
}