import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("net.fabricmc.fabric-loom-remap") apply false
    id("net.fabricmc.fabric-loom") apply false
    id("me.modmuss50.mod-publish-plugin") version "2.0.0-beta.1"
}

val isUnobfuscated = sc.current.parsed.matches(">=26.1")

if (isUnobfuscated) {
    apply(plugin = "net.fabricmc.fabric-loom")
} else {
    apply(plugin = "net.fabricmc.fabric-loom-remap")
}

version = "${project.property("mod_version")}+${sc.current.version}"
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
    val minecraftRangeStart = project.property("minecraft_range_start") as String
    val minecraftRangeEnd = (project.findProperty("minecraft_range_end") as? String)?.takeIf { it != "latest" }
    val minecraftRange = if (minecraftRangeEnd != null) {
        ">=$minecraftRangeStart <=$minecraftRangeEnd"
    } else {
        ">=$minecraftRangeStart"
    }
    val properties = mapOf(
        "version" to project.version,
        "loader_version" to project.property("loader_version"),
        "minecraft_range" to minecraftRange,
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

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds and copies artifacts into root build directory"
    from((if (isUnobfuscated) tasks.jar else tasks.named<org.gradle.jvm.tasks.Jar>("remapJar")).flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}

publishMods {
    file = (if (isUnobfuscated) tasks.jar else tasks.named<org.gradle.jvm.tasks.Jar>("remapJar")).flatMap { it.archiveFile }

    type = ReleaseType.STABLE
    displayName = "Sinkhole Restorer ${project.property("mod_version")} for Fabric ${stonecutter.current.version}"
    version = project.version.toString() + "-fabric"
    changelog = provider { rootProject.file("CHANGELOG.md").readText() }
    modLoaders.add("fabric")

    modrinth {
        projectId = project.property("modrinth_project_id") as String
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")

        minecraftVersionRange {
            start = project.property("minecraft_range_start") as String
            end = (project.findProperty("minecraft_range_end") as? String) ?: "latest"
            includeSnapshots = true
        }
    }
}
