include("reactor3-gen", "vertx-core-reactor3")

rootProject.name = "vertx-reactor"

rootDir.resolve("vertx.modules").useLines { lines ->
    lines.map { it.substringBefore("#").trim() }.filter { it.isNotEmpty() }.forEach { module ->
        include("$module-reactor3")
    }
}
