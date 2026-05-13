plugins {
    id("net.neoforged.moddev")
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
    val properties = mapOf(
        "version" to project.version,
        "forgelike_version" to project.property("forgelike_version"),
        "minecraft_range" to project.property("minecraft_range"),
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
