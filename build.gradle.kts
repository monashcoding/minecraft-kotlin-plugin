plugins {
    kotlin("jvm") version "2.1.0-RC"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.edwnl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.13.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.13.0")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    val serverPluginsDir = file("./development-server/plugins")  // Update this path!

    register<Copy>("copyToServer") {
        from(shadowJar)
        into(serverPluginsDir)
    }

    build {
        dependsOn(shadowJar)
        finalizedBy("copyToServer")
    }

    shadowJar {
        archiveFileName.set("MAC-SMP-Core.jar")
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
