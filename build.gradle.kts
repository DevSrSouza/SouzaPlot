plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
}

group = "dev.srsouza.hytale.plot"
version = "0.2.0"

val javaVersion = 25

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.bundles.exposed)
    implementation(libs.hikaricp)
    implementation(libs.h2)

    implementation(libs.slf4j.simple)

    compileOnly(libs.jsr305)
}

kotlin {
    jvmToolchain(javaVersion)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")

        mergeServiceFiles()

        manifest {
            attributes(
                "Multi-Release" to "true"
            )
        }
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }

    processResources {
        filesMatching("manifest.json") {
            expand(mapOf(
                "version" to version,
                "group" to group
            ))
        }
    }
}

// Task to copy the built plugin to the server's mods folder
tasks.register<Copy>("deployToServer") {
    dependsOn("shadowJar")
    from(tasks.shadowJar.get().archiveFile)
    into(layout.projectDirectory.dir("../test-server2/mods"))

    doLast {
        println("Plugin deployed to test-server/Mods folder")
    }
}
