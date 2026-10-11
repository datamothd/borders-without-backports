plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter.active(providers.gradleProperty("dependencies.minecraft").get())

val collectArtifacts = tasks.register<Sync>("collectArtifacts") {
    dependsOn(stonecutter.tasks.named("build"))
    from(subprojects.map { it.layout.buildDirectory.dir("libs") })
    into(layout.buildDirectory.dir("libs"))
}

tasks.register("build") {
    dependsOn(collectArtifacts)
}

tasks.register<Delete>("clean") {
    dependsOn(stonecutter.tasks.named("clean"))
    delete(layout.buildDirectory)
}
