dependencies {
    compileOnly(project(":vertx-auth-jwt-reactor3"))
    compileOnly("io.vertx:vertx-auth-jwt")
    compileOnly(project(":vertx-auth-htdigest-reactor3"))
    compileOnly("io.vertx:vertx-auth-htdigest")
    compileOnly(project(":vertx-auth-oauth2-reactor3"))
    compileOnly("io.vertx:vertx-auth-oauth2")
    compileOnly(project(":vertx-auth-otp-reactor3"))
    compileOnly("io.vertx:vertx-auth-otp")
    compileOnly(project(":vertx-auth-jwt-reactor3"))
    compileOnly("io.vertx:vertx-auth-jwt")
    compileOnly(project(":vertx-auth-webauthn-reactor3"))
    compileOnly("io.vertx:vertx-auth-webauthn")

    implementation(project(":vertx-auth-common-reactor3"))
    implementation(project(":vertx-bridge-common-reactor3"))
    implementation(project(":vertx-web-common-reactor3"))
}