import org.polyfrost.gradle.provideIncludedDependencies

plugins {
    id("gg.essential.loom")
}

dependencies {
    minecraft("com.mojang:minecraft:1.16.5")
    mappings("net.fabricmc:yarn:1.16.5+build.10:v2")

    provideIncludedDependencies(null, null).forEach {
        val dep = if (it.mod) throw IllegalArgumentException("Should not be a mod!") else compileOnly(it.dep)
        if (dep != null) {
            include(dep)
        }
    }
}

