plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

description = "The gameplay-world seam shared by Polyp and world systems (Archipelago): MechanicsWorld + tick scaling"
group = "io.github.term4"
version = rootProject.version
java.toolchain.languageVersion = JavaLanguageVersion.of(25)

mavenPublishing {
    coordinates(group.toString(), "polyp-world", version.toString())
    publishToMavenCentral()
    if (providers.gradleProperty("signingInMemoryKey").isPresent || providers.gradleProperty("signing.keyId").isPresent) {
        signAllPublications()
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    val minestomVersion = "2026.08.16-26.2"
    compileOnly("net.minestom:minestom:$minestomVersion")
    compileOnly("org.slf4j:slf4j-api:2.0.18")
}
