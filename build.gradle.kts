import java.util.*

plugins {
    kotlin("jvm") version mct.Versions.kotlin
}

group = "ca.allanwang"
version = mct.Versions.mctVersion

repositories {
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(mct.Dependencies.bukkit)
    implementation(mct.Dependencies.clikt)

    testImplementation(mct.Dependencies.hamkrest)
    testImplementation(kotlin("test-junit5"))
    testImplementation(mct.Dependencies.junit("api"))
    testImplementation(mct.Dependencies.junit("params"))
    testRuntimeOnly(mct.Dependencies.junit("engine"))
}

tasks.test {
    useJUnitPlatform()
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-Release")
    archiveVersion.set("")
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    from(
        configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

task("exportJar", type = Copy::class) {
    val props = Properties()
    file("priv.properties").inputStream().use { props.load(it) }

    from(fatJar.archiveFile)
    into(props.getProperty("export_folder"))
    dependsOn(fatJar)
}