plugins {
    id("net.fabricmc.fabric-loom-remap")
}

val minecraft = stonecutter.current.version
val modId = required("mod.id")
val modAuthor = required("mod.author")
val modGroup = "dev.${modAuthor.lowercase().replace(Regex("[^a-z0-9]"), "")}.$modId"
val modVersion = "${formatVersion("${required("mod.version")}+$minecraft")}-fabric"

base.archivesName = modId
version = modVersion
group = modGroup
loom.accessWidenerPath = rootProject.file("src/main/resources/$modId.accesswidener")

stonecutter {
    constants.putAll(mapOf("fabric" to true, "neoforge" to false, "forge" to false))
    dependencies.putAll(mapOf("sodium" to "0.8.13", "modmenu" to "11.0.3", "cloth-config" to "15.0.140", "yacl" to "3.8.2"))
    replacements {
        string(true) {
            replace("MonitorManager", "ScreenManager")
            replace("monitorManager", "screenManager")
            replace("window.handle()", "window.getWindow()")
            replace("monitor.x()", "monitor.getX()")
            replace("monitor.y()", "monitor.getY()")
            replace("monitor.currentMode()", "monitor.getCurrentMode()")
            replace("monitor.monitor()", "monitor.getMonitor()")
            replace("updateFullscreenIfChanged", "updateFullscreen")
            replace("Identifier", "ResourceLocation")
            replace("User32.GetWindowLongPtr(null, ", "User32.GetWindowLongPtr(")
            replace("User32.SetWindowLongPtr(null, ", "User32.SetWindowLongPtr(")
            replace("User32.SetWindowPos(null, ", "User32.SetWindowPos(")
        }
        regex(true) {
            replace("\\b_\\b", "ignored", "\\bignored\\b", "_")
        }
    }
}

repositories {
    maven("https://api.modrinth.com/maven/") { content { includeGroup("maven.modrinth") } }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${required("dependencies.fabric-loader")}")
    modCompileOnly("maven.modrinth:sodium:mc1.21.1-0.8.13-fabric")
    modCompileOnly("maven.modrinth:modmenu:11.0.3")
    modCompileOnly("maven.modrinth:cloth-config:15.0.140+fabric")
    modCompileOnly("maven.modrinth:yacl:3.8.2+1.21.1-fabric")
    compileOnly("ca.weblite:java-objc-bridge:1.1")
}

tasks.processResources {
    exclude("META-INF/*.toml", "META-INF/accesstransformer.cfg")
    filesMatching("pack.mcmeta") {
        expand("name" to required("mod.name"))
    }
    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "id" to modId,
            "version" to modVersion,
            "name" to required("mod.name"),
            "description" to required("mod.description"),
            "environment" to required("mod.environment"),
            "author" to modAuthor,
            "group" to modGroup,
            "license" to required("mod.license"),
            "modrinth" to required("mod.modrinth.id"),
            "curseforge" to required("mod.curseforge.id"),
            "homepage" to required("mod.sources"),
            "sources" to required("mod.sources"),
            "issues" to "${required("mod.sources")}/issues",
            "fabric_loader" to required("dependencies.fabric-loader"),
            "minecraft" to minecraft,
            "java" to "21",
        ))
        filter {
            it.replace("\">=$minecraft\"", "\"$minecraft\"")
                .replace("[\"fabric\", \"quilt\", \"neoforge\", \"forge\"]", "[\"fabric\", \"quilt\"]")
        }
    }
    filesMatching("$modId.mixins.json") {
        filter { it.replace("\"required\": true", "\"required\": true, \"compatibilityLevel\": \"JAVA_21\"") }
    }
    filesMatching("$modId.accesswidener") {
        filter { it.replace("accessWidener v2 official", "accessWidener v2 named") }
    }
}

tasks.jar {
    from(rootProject.projectDir) { include("LICENSE*") }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

fun required(key: String) = property(key) as String

fun formatVersion(version: String): String {
    val isRelease = (System.getenv("GITHUB_REF") ?: "").startsWith("refs/tags/") || System.getenv("CI_RELEASE") == "1"
    val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.ifBlank { null } ?: "0"
    return when {
        isRelease -> version
        version.contains('-') -> version.replace(Regex("^([^+]+)(.*)$"), "$1.${runNumber}$2")
        else -> version.replace(Regex("^((?:\\d+\\.)*)(\\d+)(.*)$")) { "${it.groupValues[1]}${(it.groupValues[2].toInt() + 1)}-alpha.${runNumber}${it.groupValues[3]}" }
    }
}
