plugins {
    kotlin("jvm") version "1.4.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    // https://hub.spigotmc.org/nexus/content/repositories/public/org/bukkit/bukkit/maven-metadata.xml
    implementation("org.bukkit:bukkit:1.15.2-R0.1-SNAPSHOT")
}
