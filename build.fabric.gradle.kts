import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("net.fabricmc.fabric-loom-remap") apply false
    id("net.fabricmc.fabric-loom") apply false
}

val isUnobfuscated = sc.current.parsed.matches(">=26.1")

if (isUnobfuscated) {
    apply(plugin = "net.fabricmc.fabric-loom")
} else {
    apply(plugin = "net.fabricmc.fabric-loom-remap")
}

version = "${project.property("mod_version")}+${sc.current.project}"
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String + "-fabric"
}

repositories {
}

configure<LoomGradleExtensionAPI> {
    splitEnvironmentSourceSets()

    mods {
        create("sinkhole-restorer") {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    "minecraft"("com.mojang:minecraft:${sc.current.version}")

    if (!isUnobfuscated) {
        val loom = project.extensions.getByType<LoomGradleExtensionAPI>()
        "mappings"(loom.officialMojangMappings())
    }

    val myModImplementation = if (isUnobfuscated) "implementation" else "modImplementation"

    myModImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    // myModImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")
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
        "loader_version" to project.property("loader_version"),
        "minecraft_range" to project.property("minecraft_range"),
        "required_java_int" to requiredJavaInt.toString(),
    )
    inputs.properties(properties)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }

    exclude("**/neoforge.mods.toml", "**/mods.toml")
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
