dependencies {
    codegenImplementation(project(":vertx-auth-common-reactor3"))
    codegenImplementation(project(":vertx-mongo-client-reactor3"))

    implementation(project(":vertx-auth-common-reactor3"))
    implementation(project(":vertx-mongo-client-reactor3"))
}