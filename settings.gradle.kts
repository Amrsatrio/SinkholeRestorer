pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/") {
			name = "Fabric"
		}
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.kikugie.dev/snapshots") {
			name = "KikuGie Snapshots"
		}
	}

	val loom_version: String by settings
	plugins {
		id("net.fabricmc.fabric-loom-remap") version loom_version
		id("net.fabricmc.fabric-loom") version loom_version
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.9.4"
}

stonecutter {
	create(rootProject) {
		versions("26.1", "1.21.9", "1.19.3")
	}
}