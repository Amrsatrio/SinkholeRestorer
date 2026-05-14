import me.modmuss50.mpp.ReleaseType

plugins {
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin") version "2.0.0-beta.1"
}

// val isUnobfuscated = sc.current.parsed.matches(">=26.1")

version = "${project.property("mod_version")}+${sc.current.version}"
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String + "-neoforge"
}

repositories {
}

neoForge {
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
        "[$minecraftRangeStart)"
    }
    val properties = mapOf(
        "version" to project.version,
        "forgelike_version" to project.property("forgelike_version"),
        "minecraft_range" to minecraftRange,
    )
    inputs.properties(properties)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(properties)
    }

    exclude("**/fabric.mod.json", "**/*.accesswidener", "**/mods.toml")
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
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds and copies artifacts into root build directory"
    from(layout.buildDirectory.dir("libs")) {
        include("*.jar")
        exclude("*-sources.jar")
    }
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}

publishMods {
    file = tasks.jar.map { it.archiveFile.get() }

    type = ReleaseType.STABLE
    displayName = "Sinkhole Restorer ${project.property("mod_version")} for NeoForge ${stonecutter.current.version}"
    version = project.version.toString() + "-neoforge"
    changelog = provider { rootProject.file("CHANGELOG.md").readText() }
    modLoaders.add("neoforge")

    modrinth {
        projectId = project.property("modrinth_project_id") as String
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")

        minecraftVersionRange {
            start = project.property("minecraft_range_start") as String
            end = (project.findProperty("minecraft_range_end") as? String) ?: "latest"
        }
    }
}
