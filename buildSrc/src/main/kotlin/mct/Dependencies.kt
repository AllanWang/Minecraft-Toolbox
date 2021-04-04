package mct

object Dependencies {

    val bukkit = "org.bukkit:bukkit:${Versions.bukkit}"

    fun mccoroutine(type: String): String =
        "com.github.shynixn.mccoroutine:mccoroutine-bukkit-${type}:${Versions.mccoroutine}"

    val mccoroutine: String = mccoroutine("api")

    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerKapt =
        "com.google.dagger:dagger-compiler:${Versions.dagger}"

    const val guava = "com.google.guava:guava:${Versions.guava}"

    // https://github.com/Kotlin/kotlinx.coroutines/releases
    val coroutines = coroutines("core")
    fun coroutines(type: String) =
        "org.jetbrains.kotlinx:kotlinx-coroutines-${type}:${Versions.coroutines}"

    fun sqldelight(type: String) =
        "com.squareup.sqldelight:${type}:${Versions.sqldelight}"

    fun exposed(type: String) =
        "org.jetbrains.exposed:exposed-${type}:${Versions.exposed}"

    val mysql = "mysql:mysql-connector-java:${Versions.mysql}"

    val h2 = "com.h2database:h2:${Versions.h2}"

    fun junit(type: String) =
        "org.junit.jupiter:junit-jupiter-${type}:${Versions.junit}"

    const val hamkrest = "com.natpryce:hamkrest:${Versions.hamkrest}"

    const val truth = "com.google.truth:truth:${Versions.truth}"
}