plugins {
    id("net.neoforged.moddev") version "2.0.148"
}

val minecraft = stonecutter.current.version
val neoforge = "21.1.256"
val modId = required("mod.id")
val modAuthor = required("mod.author")
val modVersion = "${formatVersion("${required("mod.version")}+$minecraft")}-neoforge"

base.archivesName = modId
version = modVersion
group = "dev.${modAuthor.lowercase().replace(Regex("[^a-z0-9]"), "")}.$modId"

stonecutter {
    constants.putAll(mapOf("fabric" to false, "neoforge" to true, "forge" to false))
    dependencies.putAll(mapOf("sodium" to "0.0.0", "modmenu" to "0.0.0", "cloth-config" to "15.0.140", "yacl" to "3.8.2"))
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
            replace("FMLLoader.getCurrent().getLoadingModList()", "FMLLoader.getLoadingModList()")
            replace("@net.neoforged.fml.common.Mod(CWB.MOD_ID)", "@net.neoforged.fml.common.Mod(value = CWB.MOD_ID, dist = net.neoforged.api.distmarker.Dist.CLIENT)")
            replace("User32.GetWindowLongPtr(null, ", "User32.GetWindowLongPtr(")
            replace("User32.SetWindowLongPtr(null, ", "User32.SetWindowLongPtr(")
            replace("User32.SetWindowPos(null, ", "User32.SetWindowPos(")
        }
        regex(true) {
            replace("\\b_\\b", "ignored", "\\bignored\\b", "_")
        }
    }
}

neoForge {
    version = neoforge
    accessTransformers.files.setFrom(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
    runs {
        register("client") {
            client()
        }
    }
    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

repositories {
    maven("https://api.modrinth.com/maven/") { content { includeGroup("maven.modrinth") } }
}

dependencies {
    compileOnly("maven.modrinth:cloth-config:15.0.140+neoforge")
    compileOnly("maven.modrinth:yacl:3.8.2+1.21.1-neoforge")
    compileOnly("ca.weblite:java-objc-bridge:1.1")
}

tasks.processResources {
    exclude("fabric.mod.json", "META-INF/mods.toml", "*.accesswidener")
    filesMatching("$modId.mixins.json") {
        filter {
            it.replace("\"required\": true", "\"required\": true, \"compatibilityLevel\": \"JAVA_21\"")
                .replace("\"WindowMixin\"", "\"WindowMixin\", \"NeoForgeWindowMixin\"")
        }
    }
    filesMatching(listOf("META-INF/*.toml", "*.mcmeta")) {
        expand(mapOf(
            "id" to modId,
            "version" to modVersion,
            "name" to required("mod.name"),
            "description" to required("mod.description"),
            "author" to modAuthor,
            "license" to required("mod.license"),
            "modrinth" to required("mod.modrinth.id"),
            "curseforge" to required("mod.curseforge.id"),
            "homepage" to required("mod.sources"),
            "issues" to "${required("mod.sources")}/issues",
            "neoforge" to neoforge,
            "minecraft" to minecraft,
        ))
    }
    filesMatching("META-INF/neoforge.mods.toml") {
        filter { it.replace("[$minecraft,)", "[$minecraft]") }
    }
}

tasks.jar {
    from(rootProject.projectDir) { include("LICENSE*") }
    manifest.attributes("MixinConfigs" to "$modId.mixins.json")
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
