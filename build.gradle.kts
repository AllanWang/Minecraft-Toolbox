import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*
import com.squareup.sqldelight.gradle.SqlDelightExtension

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(mct.Plugins.sqldelight)
    }
}

plugins {
    kotlin("jvm") version mct.Versions.kotlin
    kotlin("kapt") version mct.Versions.kotlin
}

repositories {
    jcenter()
}

apply(plugin = "com.squareup.sqldelight")

extensions.configure<SqlDelightExtension>("sqldelight") {
    database("MctDb") {
        packageName = "ca.allanwang.minecraft.toolbox.sqldelight"
        dialect = "mysql"
        migrationOutputDirectory = file("$buildDir/resources/main/migrations")
    }
}

subprojects {
    if (projectDir.name == "buildSrc") {
        return@subprojects
    }

    group = "ca.allanwang"
    version = mct.Versions.mctVersion

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
//    apply(plugin = "com.squareup.sqldelight")

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