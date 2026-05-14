plugins {
    id("dev.kikugie.stonecutter")
}

val ciSingleBuild: String? = System.getenv("CI_SINGLE_BUILD")
if (ciSingleBuild != null) {
    stonecutter active ciSingleBuild.split(":")[0]
} else {
    stonecutter active "26.1-fabric"
}

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge")
}
