// dkCore - shared core for the dk plugin suite. Author: direk james
//
//   ./gradlew build                 -> build/libs/dkCore-<version>.jar (put on the server)
//   ./gradlew publishToMavenLocal   -> lets other dk plugins compile against
//                                      com.direk:dkcore:<version>

plugins {
    java
    `maven-publish`
}

group = "com.direk"
version = "1.3.0"

repositories {
    // dkBank's Developer API: in the dkBank project run `gradlew :api:publishToMavenLocal` first.
    mavenLocal {
        content { includeGroup("dev.direk") }
    }
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots") {
        content { includeGroup("org.purpurmc.purpur") }
    }
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io") {
        content { includeGroup("com.github.MilkBowl") } // Vault API only
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:${property("purpurVersion")}")
    compileOnly("com.github.MilkBowl:VaultAPI:${property("vaultVersion")}") {
        exclude(group = "org.bukkit")
    }
    // Loaded at runtime by Paper's library loader (see plugin.yml "libraries").
    compileOnly("com.zaxxer:HikariCP:${property("hikariVersion")}")
    // dkBank stays optional at runtime: only the bank bridge touches these classes, after dkBank is enabled.
    compileOnly("dev.direk:dkBank-API:${property("dkbankApiVersion")}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false
    options.encoding = "UTF-8" // otherwise Windows reads § as a broken character
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}

tasks.jar {
    archiveFileName.set("dkCore-${project.version}.jar")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "dkcore"
            from(components["java"])
            pom {
                name.set("dkCore")
                description.set("Shared core for the dk plugin suite.")
                developers {
                    developer { name.set("direk james") }
                }
                licenses {
                    license { name.set("All Rights Reserved") }
                }
            }
        }
    }
}
