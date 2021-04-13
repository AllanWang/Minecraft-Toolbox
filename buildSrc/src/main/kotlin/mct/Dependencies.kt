package mct

object Dependencies {

    val bukkit = "org.bukkit:bukkit:${Versions.bukkit}"

    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerKapt =
        "com.google.dagger:dagger-compiler:${Versions.dagger}"

    // https://github.com/Kotlin/kotlinx.coroutines/releases
    val coroutines = coroutines("core")
    fun coroutines(type: String) =
        "org.jetbrains.kotlinx:kotlinx-coroutines-${type}:${Versions.coroutines}"

    fun sqldelight(type: String) =
        "com.squareup.sqldelight:${type}:${Versions.sqldelight}"

    val mysql = "mysql:mysql-connector-java:${Versions.mysql}"

    val h2 = "com.h2database:h2:${Versions.h2}"

    fun junit(type: String) =
        "org.junit.jupiter:junit-jupiter-${type}:${Versions.junit}"

    const val truth = "com.google.truth:truth:${Versions.truth}"
}