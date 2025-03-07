val vertxVersion: String by rootProject.ext
val reactorVersion: String by rootProject.ext

dependencies {
    implementation(platform("io.vertx:vertx-dependencies:$vertxVersion"))
    implementation(platform("io.projectreactor:reactor-bom:$reactorVersion"))
    implementation("io.projectreactor:reactor-core")
    implementation("io.vertx:vertx-codegen")
    implementation("io.vertx:vertx-docgen")
    implementation("io.vertx:vertx-rx-gen:$vertxVersion")
}
