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
    maven { url = uri("https://jitpack.io") }
    maven {
        url = uri("https://maven.pkg.github.com/Cxom/PunchTree-Util")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

val ftpAntTask by configurations.creating

dependencies {
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.19.3-R0.1-SNAPSHOT")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    compileOnly("net.punchtree:punchtree-util:0.0.1-SNAPSHOT")

    ftpAntTask("org.apache.ant:ant-commons-net:1.10.12") {
        module("commons-net:commons-net:1.4.1") {
            dependencies("oro:oro:2.0.8:jar")
        }
    }
}

tasks {
    build {
        dependsOn(reobfJar)
    }
}

val ftpHostUrl: String by project
val ftpUsername: String by project
val ftpPassword: String by project

task("uploadToServer") {
    doLast{
        ant.withGroovyBuilder {
            "taskdef"("name" to "ftp", "classname" to "org.apache.tools.ant.taskdefs.optional.net.FTP", "classpath" to ftpAntTask.asPath)
            "ftp"("server" to ftpHostUrl, "userid" to ftpUsername, "password" to ftpPassword, "remoteDir" to "/default/plugins") {
                "fileset"("dir" to "build/libs") {
                    "include"("name" to rootProject.name + "-" + version + ".jar")
                }
            }
        }
    }
}

task("buildAndPublish") {
    dependsOn("build")
    dependsOn("uploadToServer")
    tasks.findByName("uploadToServer")!!.mustRunAfter("build")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}