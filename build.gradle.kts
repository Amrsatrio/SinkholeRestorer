import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
	id("net.fabricmc.fabric-loom-remap") apply false
	id("net.fabricmc.fabric-loom") apply false
	id("maven-publish")
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
	archivesName = project.property("archives_base_name") as String
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
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

val requiredJava = when {
	sc.current.parsed.matches(">=26.1") -> JavaVersion.VERSION_25
	sc.current.parsed.matches(">=1.20.6") -> JavaVersion.VERSION_21
	sc.current.parsed.matches(">=1.18") -> JavaVersion.VERSION_17
	sc.current.parsed.matches(">=1.17") -> JavaVersion.VERSION_16
	else -> JavaVersion.VERSION_1_8
}

val requiredJavaInt = when {
	sc.current.parsed.matches(">=26.1") -> 25
    sc.current.parsed.matches(">=1.20.6") -> 21
    sc.current.parsed.matches(">=1.18") -> 17
    sc.current.parsed.matches(">=1.17") -> 16
    else -> 8
}

tasks.processResources {
	inputs.property("version", project.version)
	inputs.property("loader_version", project.property("loader_version"))
	inputs.property("minecraft_range", project.property("minecraft_range"))
	inputs.property("required_java_int", requiredJavaInt.toString())

	filesMatching("fabric.mod.json") {
		expand(
			"version" to inputs.properties["version"] as String,
			"loader_version" to inputs.properties["loader_version"] as String,
			"minecraft_range" to inputs.properties["minecraft_range"] as String,
			"required_java_int" to inputs.properties["required_java_int"] as String,
		)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = requiredJavaInt
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
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

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.property("archives_base_name") as String
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}