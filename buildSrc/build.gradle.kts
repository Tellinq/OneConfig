plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://repo.polyfrost.org/releases")
}

dependencies {
    // Mirroring https://github.com/EssentialGG/essential-gradle-toolkit/blob/master/build.gradle.kts
    implementation("org.ow2.asm:asm-commons:9.6")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
}