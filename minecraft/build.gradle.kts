@file:Suppress("UnstableApiUsage")
// Shared build logic for all versions of OneConfig.

import dev.deftu.gradle.tools.publishing.MavenPublishingExtension
import dev.deftu.gradle.utils.GameSide
import dev.deftu.gradle.utils.version.MinecraftReleaseVersion
import dev.deftu.gradle.utils.version.MinecraftVersions
import dev.deftu.gradle.utils.includeOrShade
import org.polyfrost.gradle.provideIncludedDependencies
import java.text.SimpleDateFormat

plugins {
    java
    alias(libs.plugins.kotlin)
    id(libs.plugins.dgt.multiversion.platform.get().pluginId)
    id(libs.plugins.dgt.base.get().pluginId)
    id(libs.plugins.dgt.resources.get().pluginId)
    id(libs.plugins.dgt.loom.get().pluginId)
    id(libs.plugins.dgt.shadow.get().pluginId)
}

toolkitLoomHelper {
    disableRunConfigs(GameSide.SERVER)

    useProperty("mixin.debug.export", "true", GameSide.CLIENT)
    useProperty("debugBytecode", "true", GameSide.CLIENT)
    useProperty("forge.logging.console.level", "debug", GameSide.CLIENT)
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        useProperty("fml.earlyprogresswindow", "false", GameSide.CLIENT)
    }

    if (mcData.isForge) {
        useForgeMixin(modData.id)
    }

    if (mcData.isLegacyForge) {
        useTweaker("org.polyfrost.oneconfig.internal.legacy.OneConfigTweaker")
    }
}

if (mcData.isForge) {
    loom.forge.mixinConfig("mixins.${modData.id}.init.json")
}

repositories {
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://maven.deftu.dev/releases")
}

val shadow by configurations.creating

if (mcData.isLegacyForge) { // Quick substitution for relaunch in dev env, so that mixinextras works properly (yay!)
    configurations.all {
        resolutionStrategy {
            dependencySubstitution {
                all {
                    if (requested is ModuleComponentSelector) {
                        val module = (requested as ModuleComponentSelector)
                        if (module.group == "org.ow2.asm" && module.version == "5.0.3") {
                            logger.warn("Substituting ${module.group}:${module.module}:${module.version} with ${libs.asm.get()}")
                            useTarget(module.group + ":" + module.module + ":" + libs.asm.get().version)
                        }
                    }
                }
            }
        }
    }
}

dependencies {
    compileOnly("gg.essential:vigilance-1.8.9-forge:295") {
        isTransitive = false
    }

    val mcVersion = mcData.version as MinecraftReleaseVersion
    provideIncludedDependencies(Triple(mcVersion.major, mcVersion.minor, mcVersion.patch), mcData.loader.friendlyString).forEach {
        if (it.dep is String) {
            shade(it.dep as String, it.mod)
        } else {
            shade(it.dep as ExternalModuleDependency, it.mod)
        }
    }

    annotationProcessor(libs.mixin.extras)

    for (dep in listOf("-nanovg").run { if (mcData.version < MinecraftVersions.VERSION_1_13) this else this + listOf("-tinyfd", "-stb", "") }) {
        val lwjglDep = "org.lwjgl:lwjgl$dep:${libs.versions.lwjgl.get()}"
        compileOnlyApi(lwjglDep) {
            isTransitive = false
        }
    }

    implementation(project(":modules:dependencies:bundled"))
    implementation(project(":modules:internal")) {
        isTransitive = false
    }

    if (mcData.isLegacyForge) {
        compileOnly("cc.polyfrost:oneconfig-$mcData:0.2.2-alpha216") {
            isTransitive = false
        }
    }
    if (mcData.isLegacyForge || mcData.isLegacyFabric) {
        shade("com.mojang:brigadier:1.0.18")
    }
}

fun DependencyHandlerScope.shade(dependency: String, isMod: Boolean = false) {
    val dep = project.dependencies.create(dependency) as ExternalModuleDependency
    shade(dep, isMod)
}

fun DependencyHandlerScope.shade(dependency: Provider<MinimalExternalModuleDependency>, isMod: Boolean = false) {
    shade(dependency.get(), isMod)
}

fun DependencyHandlerScope.shade(dependency: ExternalModuleDependency, isMod: Boolean = false) {
    val dep = "${dependency.group}:${dependency.name}:${dependency.version}"
    val configuration = if (isMod) modApi(dep) {
        isTransitive = false
    } else api(dep) {
        isTransitive = false
    }

    includeOrShade(configuration)
}

tasks {
    remapJar {
        manifest {
            val attributesMap = buildMap<String, Any> {
                putAll(
                    mapOf(
                        "Specification-Title" to modData.id,
                        "Specification-Vendor" to "Polyfrost",
                        "Specification-Version" to "1", // We are version 1 of ourselves, whatever the hell that means
                        "Implementation-Title" to rootProject.name,
                        "Implementation-Version" to project.version,
                        "Implementation-Vendor" to "Polyfrost",
                        "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(`java.util`.Date()),
                        "OneConfig-Main-Class" to "org.polyfrost.oneconfig.internal.bootstrap.Bootstrap"
                    )
                )
            }

            attributes(attributesMap)
        }
    }
}

configure<MavenPublishingExtension> {
    artifactName.set(project.name)
}

publishing {
    publications {
        register<MavenPublication>("java") {
            from(components["java"])

            groupId = group.toString()
            artifactId = base.archivesName.get()

            signing {
                isRequired = project.properties["signing.keyId"] != null
                sign(this@register)
            }
        }
    }
}