import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version mct.Versions.kotlin
    kotlin("kapt") version mct.Versions.kotlin
}

repositories {
    jcenter()
}

subprojects {
    if (projectDir.name == "buildSrc") {
        return@subprojects
    }

    group = "ca.allanwang"
    version = mct.Versions.mctVersion

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")


    repositories {
        jcenter()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
    }

    dependencies {
        implementation(kotlin("stdlib"))

        testImplementation(mct.Dependencies.hamkrest)
        testImplementation(kotlin("test-junit5"))
        testImplementation(mct.Dependencies.junit("api"))
        testImplementation(mct.Dependencies.junit("params"))
        testRuntimeOnly(mct.Dependencies.junit("engine"))
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = mct.Gradle.jvmTarget
            freeCompilerArgs = mct.Gradle.compilerArgs
        }
    }

}