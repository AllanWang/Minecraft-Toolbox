package mct

object Dependencies {

    val slf4j = slf4j("api")
    fun slf4j(type: String) = "org.slf4j:slf4j-${type}:${Versions.slf4j}"

    val flogger = "com.google.flogger:flogger:${Versions.flogger}"
    fun flogger(type: String) = "com.google.flogger:flogger-${type}:${Versions.flogger}"

    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerKapt = "com.google.dagger:dagger-compiler:${Versions.dagger}"

    // https://github.com/Kotlin/kotlinx.coroutines/releases
    val coroutines = coroutines("core")
    fun coroutines(type: String) = "org.jetbrains.kotlinx:kotlinx-coroutines-${type}:${Versions.coroutines}"

    fun junit(type: String) = "org.junit.jupiter:junit-jupiter-${type}:${Versions.junit}"

    const val hamkrest = "com.natpryce:hamkrest:${Versions.hamkrest}"
}