dependencies {
    compileOnly(project(":vertx-auth-oauth2-reactor3", "default")) // TODO Check if it's compile only

    implementation(project(":vertx-uri-template-reactor3"))
    implementation(project(":vertx-web-common-reactor3"))
}