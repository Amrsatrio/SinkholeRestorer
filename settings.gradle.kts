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
    val neoforge_moddev_version: String by settings
    plugins {
        id("net.fabricmc.fabric-loom-remap") version loom_version
        id("net.fabricmc.fabric-loom") version loom_version
        id("net.neoforged.moddev") version neoforge_moddev_version
        id("net.neoforged.moddev.legacyforge") version neoforge_moddev_version
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    val ciSingleBuild: String? = System.getenv("CI_SINGLE_BUILD")
    if (ciSingleBuild != null) {
        val split = ciSingleBuild.split(":")
        create(rootProject) {
            version(split[0], split[1]).buildscript(split[2])
        }
    } else {
        create(rootProject, file("versions.json"))
    }
}
