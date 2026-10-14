pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("net.fabricmc.fabric-loom") version providers.gradleProperty("dependencies.loom")
        id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("dependencies.loom")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    create(rootProject) {
        version(providers.gradleProperty("dependencies.minecraft").get())
        version("1.21.1-neoforge", "1.21.1").buildscript("build-neoforge.gradle.kts")
        version("1.21.1-fabric", "1.21.1").buildscript("build-fabric.gradle.kts")
    }
}
