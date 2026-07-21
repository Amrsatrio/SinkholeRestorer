import me.modmuss50.mpp.ReleaseType

plugins {
    id("net.neoforged.moddev.legacyforge")
    id("me.modmuss50.mod-publish-plugin") version "2.0.0-beta.1"
}

// val isUnobfuscated = sc.current.parsed.matches(">=26.1")

version = "${project.property("mod_version")}+${sc.current.version}"
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String + "-forge"
}

repositories {
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

legacyForge {
    version = project.property("forgelike_version") as String
    validateAccessTransformers = true

    runs {
        register("client") {
            gameDirectory = file("run/")
            client()
        }
        register("server") {
            gameDirectory = file("run/")
            server()
        }
    }

    mods {
        create("sinkhole_restorer") {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    add(sourceSets.getByName("main"), "sinkhole-restorer.mixins.refmap.json")
    config("sinkhole-restorer.mixins.json")
}

val (requiredJava, requiredJavaInt) = when {
    sc.current.parsed.matches(">=26.1") -> JavaVersion.VERSION_25 to 25
    sc.current.parsed.matches(">=1.20.6") -> JavaVersion.VERSION_21 to 21
    sc.current.parsed.matches(">=1.18") -> JavaVersion.VERSION_17 to 17
    sc.current.parsed.matches(">=1.17") -> JavaVersion.VERSION_16 to 16
    else -> JavaVersion.VERSION_1_8 to 8
}

tasks.processResources {
    val minecraftRangeStart = project.property("minecraft_range_start") as String
    val minecraftRangeEnd = (project.findProperty("minecraft_range_end") as? String)?.takeIf { it != "latest" }
    val minecraftRange = if (minecraftRangeEnd != null) {
        "[$minecraftRangeStart,$minecraftRangeEnd]"
    } else {
        "[$minecraftRangeStart,)"
    }
    val properties = mapOf(
        "version" to project.version,
        "forgelike_version" to project.property("forgelike_version"),
        "minecraft_range" to minecraftRange,
    )
    inputs.properties(properties)
    filesMatching("META-INF/mods.toml") {
        expand(properties)
    }

    exclude("**/fabric.mod.json", "**/*.accesswidener", "**/neoforge.mods.toml")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJavaInt
}

java {
    withSourcesJar()

    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}"}
    }

    manifest.attributes(mapOf(
        "MixinConfigs" to "sinkhole-restorer.mixins.json"
    ))
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds and copies artifacts into root build directory"
    from(tasks.named<org.gradle.jvm.tasks.Jar>("reobfJar").flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}

publishMods {
    file = tasks.named<org.gradle.jvm.tasks.Jar>("reobfJar").flatMap { it.archiveFile }

    type = ReleaseType.STABLE
    displayName = "Sinkhole Restorer ${project.property("mod_version")} for Forge ${stonecutter.current.version}"
    version = project.version.toString() + "-forge"
    changelog = provider { rootProject.file("CHANGELOG.md").readText() }
    modLoaders.add("forge")

    modrinth {
        projectId = project.property("modrinth_project_id") as String
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")

        minecraftVersionRange {
            start = project.property("minecraft_range_start_publish") as String
            end = (project.findProperty("minecraft_range_end_publish") as? String) ?: "latest"
        }
    }
}
