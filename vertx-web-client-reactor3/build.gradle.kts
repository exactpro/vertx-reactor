dependencies {
    // TODO Check if these are compile only
    compileOnly(project(":vertx-auth-oauth2-reactor3"))
    compileOnly("io.vertx:vertx-auth-oauth2")

    implementation(project(":vertx-uri-template-reactor3"))
    implementation(project(":vertx-web-common-reactor3"))
}