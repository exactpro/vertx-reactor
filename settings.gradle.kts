include("reactor3-gen", "vertx-core-reactor3")

rootProject.name = "vertx-reactor"

rootDir.resolve("vertx.modules").useLines { lines ->
    lines.filter { it.isNotBlank() }.forEach { module ->
        include("$module-reactor3")
    }
}