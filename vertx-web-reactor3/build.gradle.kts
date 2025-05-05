dependencies {
    codegenCompileOnly(project(":vertx-auth-jwt-reactor3", "default"))
    codegenCompileOnly(project(":vertx-auth-htdigest-reactor3", "default"))
    codegenCompileOnly(project(":vertx-auth-oauth2-reactor3", "default"))
    codegenCompileOnly(project(":vertx-auth-otp-reactor3", "default"))
    codegenCompileOnly(project(":vertx-auth-jwt-reactor3", "default"))
    codegenCompileOnly(project(":vertx-auth-webauthn-reactor3", "default"))

    codegenImplementation(project(":vertx-auth-common-reactor3"))
    codegenImplementation(project(":vertx-bridge-common-reactor3"))
    codegenImplementation(project(":vertx-web-common-reactor3"))

    compileOnly(project(":vertx-auth-jwt-reactor3", "default"))
    compileOnly(project(":vertx-auth-htdigest-reactor3", "default"))
    compileOnly(project(":vertx-auth-oauth2-reactor3", "default"))
    compileOnly(project(":vertx-auth-otp-reactor3", "default"))
    compileOnly(project(":vertx-auth-jwt-reactor3", "default"))
    compileOnly(project(":vertx-auth-webauthn-reactor3", "default"))

    implementation(project(":vertx-auth-common-reactor3"))
    implementation(project(":vertx-bridge-common-reactor3"))
    implementation(project(":vertx-web-common-reactor3"))
}