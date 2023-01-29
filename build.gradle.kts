plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.4.1"
}

group = "net.punchtree"
version = "1.0-SNAPSHOT"
description = "Custom functionality for the PunchTree freebuild server."

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.19.3-R0.1-SNAPSHOT")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
}

tasks {
    build {
        dependsOn(reobfJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}