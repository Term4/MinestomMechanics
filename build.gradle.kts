plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.36.0"
}

description = "A library for Minestom 1.8 mechanics"
group = "io.github.Term4"
version = "0.1.0"
java.toolchain.languageVersion = JavaLanguageVersion.of(25)

mavenPublishing {
    coordinates(group.toString(), "minestom-mechanics", version.toString())
    publishToMavenCentral()
    signAllPublications()

    pom {
        name = "minestom-mechanics"
        description = project.description
        url = "https://github.com/Term4/MinestomMechanics"

        // I think maven central requires a license and email for publishing but I just commented them out

//        licenses {
//            license {
//                name = "Apache-2.0"
//                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
//            }
//        }

        developers {
            developer {
                name = "Term4"
                id = "Term4"
                email = "gptkc2003@gmail.com"
                url = "https://github.com/Term4"
            }
        }

        scm {
            url = "https://github.com/Term4/MinestomMechanics"
            connection = "scm:git:git://github.com/Term4/MinestomMechanics.git"
            developerConnection = "scm:git:ssh://git@github.com/Term4/MinestomMechanics.git"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val minestomVersion = "2026.02.19-1.21.11"
    val junitVersion = "6.0.3"

    compileOnly("net.minestom:minestom:$minestomVersion")

    // Unit testing
    testImplementation("net.minestom:minestom:$minestomVersion")
    testImplementation("org.tinylog:tinylog-api:2.8.0-M1")
    testImplementation("org.tinylog:tinylog-impl:2.8.0-M1")
    testImplementation("org.tinylog:slf4j-tinylog:2.8.0-M1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
    failOnNoDiscoveredTests.set(false)
}