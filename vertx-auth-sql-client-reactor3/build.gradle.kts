dependencies {
    codegenImplementation(project(":vertx-auth-common-reactor3"))
    codegenImplementation(project(":vertx-sql-client-reactor3"))

    implementation(project(":vertx-auth-common-reactor3"))
    implementation(project(":vertx-sql-client-reactor3"))
}