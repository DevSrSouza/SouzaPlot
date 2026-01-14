plugins {
    kotlin("jvm") version "2.3.20-Beta1"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "dev.srsouza.hytale.plot"
version = "1.0.0-SNAPSHOT"

val javaVersion = 25

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))

    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    implementation("org.jetbrains.exposed:exposed-core:0.46.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.46.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.46.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.h2database:h2:2.2.224")

    implementation("org.slf4j:slf4j-simple:2.0.9")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
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
    into(layout.projectDirectory.dir("../test-server/Mods"))

    doLast {
        println("Plugin deployed to test-server/Mods folder")
    }
}
